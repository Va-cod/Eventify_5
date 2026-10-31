package com.eventify_4.service;

import com.eventify_4.dto.request.EventRequestDTO;
import com.eventify_4.dto.response.EventResponseDTO;
import com.eventify_4.dto.response.EventSummaryDTO;
import com.eventify_4.entity.Category;
import com.eventify_4.entity.Event;
import com.eventify_4.entity.Venue;
import com.eventify_4.exception.EventNotFoundException;
import com.eventify_4.exception.InvalidCategoryException;
import com.eventify_4.exception.InvalidEventDateException;
import com.eventify_4.exception.VenueAlreadyBookedException;
import com.eventify_4.exception.VenueNotFoundException;
import com.eventify_4.repository.CategoryRepository;
import com.eventify_4.repository.EventRepository;
import com.eventify_4.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Recupera una porción (Slice) de eventos activos optimizando la latencia (sin COUNT(*))
     * y cargando relaciones en una sola consulta para evitar N+1.
     */
    public Slice<EventSummaryDTO> getEventsSlice(Pageable pageable) {
        return eventRepository.findAllWithVenueAndCategory(pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Filtrar eventos por ciudad del recinto con ordenamiento cronológico descendente.
     */
    public Slice<EventSummaryDTO> searchByCity(String city, Pageable pageable) {
        return eventRepository.findByVenue_CityIgnoreCaseOrderByDateDesc(city, pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Filtrar eventos por nombre de categoría con ordenamiento cronológico descendente.
     */
    public Slice<EventSummaryDTO> searchByCategory(String categoryName, Pageable pageable) {
        return eventRepository.findByCategory_NameIgnoreCaseOrderByDateDesc(categoryName, pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Filtrar eventos por rango de fechas con ordenamiento cronológico descendente.
     */
    public Slice<EventSummaryDTO> searchByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidEventDateException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return eventRepository.findByDateBetweenOrderByDateDesc(startDate, endDate, pageable)
                .map(this::mapToSummaryDTO);
    }

    /**
     * Obtener el detalle de un evento por su ID asegurando que venue y category se carguen en 1 query.
     */
    public EventSummaryDTO getEventById(Long id) {
        return eventRepository.findByIdWithDetails(id)
                .map(this::mapToSummaryDTO)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    /**
     * Obtener respuesta detallada de evento para la interfaz web.
     */
    public EventResponseDTO getEventResponseById(Long id) {
        return eventRepository.findByIdWithDetails(id)
                .map(this::mapToResponseDTO)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    /**
     * Listar todos los eventos activos con sus relaciones cargadas.
     */
    public List<EventResponseDTO> getAllEvents() {
        return eventRepository.findAllWithVenueAndCategory().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    /**
     * Búsqueda y filtrado para la interfaz web.
     */
    public List<EventResponseDTO> searchEvents(String city, String category, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidEventDateException("La fecha inicial no puede ser posterior a la fecha final.");
        }

        List<Event> events;
        if (city != null && !city.isBlank()) {
            events = eventRepository.findByVenue_CityIgnoreCaseOrderByDateDesc(city, Pageable.unpaged()).getContent();
        } else if (category != null && !category.isBlank()) {
            events = eventRepository.findByCategory_NameIgnoreCaseOrderByDateDesc(category, Pageable.unpaged()).getContent();
        } else if (startDate != null || endDate != null) {
            events = eventRepository.findByDateBetweenOrderByDateDesc(startDate, endDate, Pageable.unpaged()).getContent();
        } else {
            events = eventRepository.findAllWithVenueAndCategory();
        }

        return events.stream().map(this::mapToResponseDTO).toList();
    }

    /**
     * Contar total de eventos activos.
     */
    public long countEvents() {
        return eventRepository.count();
    }

    /**
     * Registrar un nuevo evento validando reglas de negocio:
     * 1. Fecha no puede ser pasada.
     * 2. El recinto debe existir.
     * 3. El recinto no debe estar reservado en la misma fecha (VenueAlreadyBookedException).
     * 4. La categoría debe existir.
     */
    @Transactional
    public EventSummaryDTO createEvent(EventRequestDTO request) {
        validateEventDate(request.date());

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new VenueNotFoundException(request.venueId()));

        if (eventRepository.existsByVenue_IdAndDate(venue.getId(), request.date())) {
            throw new VenueAlreadyBookedException(
                    "El lugar '" + venue.getName() + "' ya cuenta con un evento reservado para la fecha: " + request.date()
            );
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new InvalidCategoryException("Categoría no encontrada con id: " + request.categoryId()));

        Event event = Event.builder()
                .name(request.name())
                .date(request.date())
                .description(request.description())
                .venue(venue)
                .category(category)
                .active(true)
                .build();

        Event savedEvent = eventRepository.save(event);
        return mapToSummaryDTO(savedEvent);
    }

    /**
     * Actualizar un evento existente aplicando validaciones cruzadas.
     */
    @Transactional
    public EventSummaryDTO updateEvent(Long id, EventRequestDTO request) {
        Event event = eventRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new EventNotFoundException(id));

        validateEventDate(request.date());

        Venue venue = event.getVenue();
        if (!venue.getId().equals(request.venueId())) {
            venue = venueRepository.findById(request.venueId())
                    .orElseThrow(() -> new VenueNotFoundException(request.venueId()));
        }

        // Si cambió la fecha o el recinto, verificar colisión de agenda
        boolean dateOrVenueChanged = !event.getDate().equals(request.date()) || !event.getVenue().getId().equals(request.venueId());
        if (dateOrVenueChanged && eventRepository.existsByVenue_IdAndDate(venue.getId(), request.date())) {
            throw new VenueAlreadyBookedException(
                    "El lugar '" + venue.getName() + "' ya cuenta con un evento reservado para la fecha: " + request.date()
            );
        }

        Category category = event.getCategory();
        if (!category.getId().equals(request.categoryId())) {
            category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new InvalidCategoryException("Categoría no encontrada con id: " + request.categoryId()));
        }

        event.setName(request.name());
        event.setDate(request.date());
        event.setDescription(request.description());
        event.setVenue(venue);
        event.setCategory(category);

        Event updatedEvent = eventRepository.save(event);
        return mapToSummaryDTO(updatedEvent);
    }

    /**
     * Eliminación de un evento aplicando Soft Delete (respeta @SQLDelete configurado en Event).
     */
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException(id);
        }
        eventRepository.deleteById(id);
    }

    private void validateEventDate(LocalDate date) {
        if (date == null || date.isBefore(LocalDate.now())) {
            throw new InvalidEventDateException("La fecha del evento debe ser igual o posterior al día actual.");
        }
    }

    private EventSummaryDTO mapToSummaryDTO(Event event) {
        return new EventSummaryDTO(
                event.getId(),
                event.getName(),
                event.getDate(),
                event.getVenue().getName(),
                event.getVenue().getCity(),
                event.getCategory() != null ? event.getCategory().getName() : null
        );
    }

    private EventResponseDTO mapToResponseDTO(Event event) {
        return new EventResponseDTO(
                event.getId(),
                event.getName(),
                event.getDate(),
                event.getDescription(),
                event.getActive(),
                event.getVenue().getId(),
                event.getVenue().getName(),
                event.getVenue().getCity(),
                event.getVenue().getCapacity(),
                event.getCategory().getId(),
                event.getCategory().getName()
        );
    }
}
