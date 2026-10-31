package com.eventify_4.service;

import com.eventify_4.dto.request.EventCreateDTO;
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
import com.eventify_4.mapper.EventMapper;
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
    private final EventMapper eventMapper;

    /**
     * Recupera una porción (Slice) de eventos activos optimizando la latencia (sin COUNT(*))
     * y cargando relaciones en una sola consulta para evitar N+1.
     * Mantiene EventSummaryDTO como proyección optimizada para listados masivos.
     */
    public Slice<EventSummaryDTO> getEventsSlice(Pageable pageable) {
        return eventRepository.findAllWithVenueAndCategory(pageable)
                .map(eventMapper::toSummaryDTO);
    }

    /**
     * Filtrar eventos por ciudad del recinto con ordenamiento cronológico descendente.
     */
    public Slice<EventSummaryDTO> searchByCity(String city, Pageable pageable) {
        return eventRepository.findByVenue_CityIgnoreCaseOrderByDateDesc(city, pageable)
                .map(eventMapper::toSummaryDTO);
    }

    /**
     * Filtrar eventos por nombre de categoría con ordenamiento cronológico descendente.
     */
    public Slice<EventSummaryDTO> searchByCategory(String categoryName, Pageable pageable) {
        return eventRepository.findByCategory_NameIgnoreCaseOrderByDateDesc(categoryName, pageable)
                .map(eventMapper::toSummaryDTO);
    }

    /**
     * Filtrar eventos por rango de fechas con ordenamiento cronológico descendente.
     */
    public Slice<EventSummaryDTO> searchByDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new InvalidEventDateException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return eventRepository.findByDateBetweenOrderByDateDesc(startDate, endDate, pageable)
                .map(eventMapper::toSummaryDTO);
    }

    /**
     * Obtener el detalle de un evento por su ID retornando EventResponseDTO sin exponer entidades JPA.
     */
    public EventResponseDTO getEventById(Long id) {
        return eventRepository.findByIdWithDetails(id)
                .map(eventMapper::toResponseDTO)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    /**
     * Proyección compacta para cuando se requiera explícitamente el resumen.
     */
    public EventSummaryDTO getEventSummaryById(Long id) {
        return eventRepository.findByIdWithDetails(id)
                .map(eventMapper::toSummaryDTO)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    /**
     * Obtener respuesta detallada de evento para la interfaz web.
     */
    public EventResponseDTO getEventResponseById(Long id) {
        return getEventById(id);
    }

    /**
     * Listar todos los eventos activos con sus relaciones cargadas en formato DTO.
     */
    public List<EventResponseDTO> getAllEvents() {
        return eventMapper.toResponseDTOList(eventRepository.findAllWithVenueAndCategory());
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

        return eventMapper.toResponseDTOList(events);
    }

    /**
     * Contar total de eventos activos.
     */
    public long countEvents() {
        return eventRepository.count();
    }

    /**
     * Registrar un nuevo evento validando reglas de negocio y mapeando con MapStruct:
     * 1. Fecha no puede ser pasada.
     * 2. El recinto debe existir.
     * 3. El recinto no debe estar reservado en la misma fecha (VenueAlreadyBookedException).
     * 4. La categoría debe existir.
     */
    @Transactional
    public EventResponseDTO createEvent(EventCreateDTO request) {
        validateEventDate(request.date());

        Venue venue = venueRepository.findById(request.venueId())
                .orElseThrow(() -> new VenueNotFoundException(request.venueId()));

        if (eventRepository.existsByVenue_IdAndDate(venue.getId(), request.date())) {
            throw new VenueAlreadyBookedException(
                    "El lugar '" + venue.getName() + "' ya cuenta con un evento reservado para la fecha: " + request.date()
            );
        }

        Long categoryId = request.categoryId() != null ? request.categoryId()
                : (request.categoryIds() != null && !request.categoryIds().isEmpty() ? request.categoryIds().get(0) : null);
        if (categoryId == null) {
            throw new InvalidCategoryException("Debe proporcionar al menos una categoría válida.");
        }

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new InvalidCategoryException("Categoría no encontrada con id: " + categoryId));

        Event event = eventMapper.toEntity(request);
        event.setVenue(venue);
        event.setCategory(category);
        event.setActive(true);

        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponseDTO(savedEvent);
    }

    /**
     * Sobrecarga de compatibilidad para EventRequestDTO.
     */
    @Transactional
    public EventResponseDTO createEvent(EventRequestDTO request) {
        return createEvent(new EventCreateDTO(
                request.name(),
                request.date(),
                request.description(),
                request.venueId(),
                request.categoryId()
        ));
    }

    /**
     * Actualizar un evento existente aplicando validaciones cruzadas y mapeo con MapStruct.
     */
    @Transactional
    public EventResponseDTO updateEvent(Long id, EventCreateDTO request) {
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

        Long categoryId = request.categoryId() != null ? request.categoryId()
                : (request.categoryIds() != null && !request.categoryIds().isEmpty() ? request.categoryIds().get(0) : null);
        if (categoryId == null) {
            throw new InvalidCategoryException("Debe proporcionar al menos una categoría válida.");
        }

        Category category = event.getCategory();
        if (!category.getId().equals(categoryId)) {
            category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new InvalidCategoryException("Categoría no encontrada con id: " + categoryId));
        }

        eventMapper.updateEntityFromDTO(request, event);
        event.setVenue(venue);
        event.setCategory(category);

        Event updatedEvent = eventRepository.save(event);
        return eventMapper.toResponseDTO(updatedEvent);
    }

    /**
     * Sobrecarga de compatibilidad para EventRequestDTO.
     */
    @Transactional
    public EventResponseDTO updateEvent(Long id, EventRequestDTO request) {
        return updateEvent(id, new EventCreateDTO(
                request.name(),
                request.date(),
                request.description(),
                request.venueId(),
                request.categoryId()
        ));
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
}
