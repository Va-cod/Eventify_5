package com.eventify_4.service;

import com.eventify_4.dto.request.VenueCreateDTO;
import com.eventify_4.dto.request.VenueResquestDTO;
import com.eventify_4.dto.response.VenueResponseDTO;
import com.eventify_4.entity.Venue;
import com.eventify_4.exception.VenueCapacityException;
import com.eventify_4.exception.VenueNotFoundException;
import com.eventify_4.mapper.VenueMapper;
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
    private final VenueMapper venueMapper;

    public List<VenueResponseDTO> getAllVenues() {
        return venueMapper.toResponseDTOList(venueRepository.findAll());
    }

    public VenueResponseDTO getVenueById(Long id) {
        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException(id));
        return venueMapper.toResponseDTO(venue);
    }

    public long countVenues() {
        return venueRepository.count();
    }

    @Transactional
    public VenueResponseDTO createVenue(VenueCreateDTO request) {
        validateCapacity(request.capacity());

        Venue venue = venueMapper.toEntity(request);
        Venue savedVenue = venueRepository.save(venue);
        return venueMapper.toResponseDTO(savedVenue);
    }

    @Transactional
    public VenueResponseDTO createVenue(VenueResquestDTO request) {
        return createVenue(new VenueCreateDTO(request.name(), request.address(), request.capacity(), request.city()));
    }

    @Transactional
    public VenueResponseDTO updateVenue(Long id, VenueCreateDTO request) {
        validateCapacity(request.capacity());

        Venue venue = venueRepository.findById(id)
                .orElseThrow(() -> new VenueNotFoundException(id));

        venueMapper.updateEntityFromDTO(request, venue);
        Venue updatedVenue = venueRepository.save(venue);
        return venueMapper.toResponseDTO(updatedVenue);
    }

    @Transactional
    public VenueResponseDTO updateVenue(Long id, VenueResquestDTO request) {
        return updateVenue(id, new VenueCreateDTO(request.name(), request.address(), request.capacity(), request.city()));
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
}
