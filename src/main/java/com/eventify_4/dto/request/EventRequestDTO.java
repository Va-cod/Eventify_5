package com.eventify_4.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record EventRequestDTO(
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

        @NotNull(message = "El ID de la categoría es obligatorio")
        Long categoryId
) { }
