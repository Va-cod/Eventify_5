package com.eventify_4.service;

import com.eventify_4.dto.request.VenueResquestDTO;
import com.eventify_4.dto.response.VenueResponseDTO;
import com.eventify_4.entity.Venue;
import com.eventify_4.exception.VenueCapacityException;
import com.eventify_4.exception.VenueNotFoundException;
import com.eventify_4.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;

    public List<VenueResponseDTO> getAllVenues() {
        return venueRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public VenueResponseDTO getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException(id));
        return mapToResponseDTO(venue);
    }

    public long countVenues() {
        return venueRepository.count();
    }

    @Transactional
    public VenueResponseDTO createVenue(VenueResquestDTO request) {
        validateCapacity(request.capacity());

        Venue venue = Venue.builder()
                .name(request.name())
                .address(request.address())
                .capacity(request.capacity())
                .city(request.city())
                .status(true)
                .build();

        Venue savedVenue = venueRepository.save(venue);
        return mapToResponseDTO(savedVenue);
    }

    @Transactional
    public VenueResponseDTO updateVenue(Long id, VenueResquestDTO request) {
        validateCapacity(request.capacity());

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException(id));

        venue.setName(request.name());
        venue.setAddress(request.address());
        venue.setCapacity(request.capacity());
        venue.setCity(request.city());

        Venue updatedVenue = venueRepository.save(venue);
        return mapToResponseDTO(updatedVenue);
    }

    @Transactional
    public void deleteVenue(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException(id));
        venueRepository.delete(venue);
    }

    private void validateCapacity(Integer capacity) {
        if (capacity == null || capacity <= 0) {
            throw new VenueCapacityException("La capacidad del lugar debe ser un número entero mayor a cero.");
        }
    }

    private VenueResponseDTO mapToResponseDTO(Venue venue) {
        return new VenueResponseDTO(
                venue.getId(),
                venue.getName(),
                venue.getAddress(),
                venue.getCapacity(),
                venue.getCity()
        );
    }
}
