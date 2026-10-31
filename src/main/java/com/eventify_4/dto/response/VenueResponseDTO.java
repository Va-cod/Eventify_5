package com.eventify_4.dto.response;

public record VenueResponseDTO(
        Long id,
        String name,
        String address,
        Integer capacity,
        String city
) {
}
