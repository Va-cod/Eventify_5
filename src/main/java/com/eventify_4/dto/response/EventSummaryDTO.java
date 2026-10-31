package com.eventify_4.dto.response;

import java.time.LocalDate;

public record EventSummaryDTO(
        Long id,
        String eventName,
        LocalDate eventDate,
        String venueName,
        String city,
        String categoryName
) {
    public EventSummaryDTO(String eventName, LocalDate eventDate, String venueName, String city) {
        this(null, eventName, eventDate, venueName, city, null);
    }
}
