package com.eventify_4.dto.response;

import java.time.LocalDate;

public record EventResponseDTO(
        Long id,
        String name,
        LocalDate date,
        String description,
        Boolean active,
        Long venueId,
        String venueName,
        String venueCity,
        Integer venueCapacity,
        Long categoryId,
        String categoryName
) {
}
