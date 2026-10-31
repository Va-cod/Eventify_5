package com.eventify_4.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public record EventCreateDTO(
        @NotBlank(message = "El nombre del evento es obligatorio")
        @Size(max = 150, message = "El nombre no puede superar los 150 caracteres")
        String name,

        @NotNull(message = "La fecha del evento es obligatoria")
        @FutureOrPresent(message = "La fecha debe ser presente o futura")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        String description,

        @NotNull(message = "El ID del lugar (venue) es obligatorio")
        Long venueId,

        List<Long> categoryIds,

        Long categoryId
) {
    public EventCreateDTO {
        if (categoryIds == null || categoryIds.isEmpty()) {
            if (categoryId != null) {
                categoryIds = List.of(categoryId);
            } else {
                categoryIds = Collections.emptyList();
            }
        } else if (categoryId == null && !categoryIds.isEmpty()) {
            categoryId = categoryIds.get(0);
        }
    }

    public EventCreateDTO(String name, LocalDate date, String description, Long venueId, List<Long> categoryIds) {
        this(name, date, description, venueId, categoryIds, (categoryIds != null && !categoryIds.isEmpty()) ? categoryIds.get(0) : null);
    }

    public EventCreateDTO(String name, LocalDate date, String description, Long venueId, Long categoryId) {
        this(name, date, description, venueId, categoryId != null ? List.of(categoryId) : Collections.emptyList(), categoryId);
    }
}
