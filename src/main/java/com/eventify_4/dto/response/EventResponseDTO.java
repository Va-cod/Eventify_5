package com.eventify_4.dto.response;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record EventResponseDTO(
        Long id,
        String name,
        LocalDate date,
        String description,
        String venueName,
        List<String> categoryNames,
        Boolean active,
        Long venueId,
        String venueCity,
        Integer venueCapacity,
        Long categoryId,
        String categoryName
) {
    public EventResponseDTO {
        if (categoryNames == null || categoryNames.isEmpty()) {
            if (categoryName != null) {
                categoryNames = List.of(categoryName);
            } else {
                categoryNames = Collections.emptyList();
            }
        } else if (categoryName == null && !categoryNames.isEmpty()) {
            categoryName = categoryNames.get(0);
        }
    }

    public EventResponseDTO(Long id, String name, LocalDate date, String description, String venueName, List<String> categoryNames) {
        this(id, name, date, description, venueName, categoryNames, true, null, null, null, null,
                (categoryNames != null && !categoryNames.isEmpty()) ? categoryNames.get(0) : null);
    }

    public EventResponseDTO(Long id, String name, LocalDate date, String description, Boolean active,
                            Long venueId, String venueName, String venueCity, Integer venueCapacity,
                            Long categoryId, String categoryName) {
        this(id, name, date, description, venueName,
                categoryName != null ? List.of(categoryName) : Collections.emptyList(),
                active, venueId, venueCity, venueCapacity, categoryId, categoryName);
    }
}
