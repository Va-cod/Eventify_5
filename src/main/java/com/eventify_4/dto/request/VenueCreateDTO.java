package com.eventify_4.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record VenueCreateDTO(
        @NotBlank(message = "El nombre del lugar es obligatorio")
        @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
        String name,

        @NotBlank(message = "La dirección del lugar es obligatoria")
        @Size(max = 80, message = "La dirección no puede superar los 80 caracteres")
        String address,

        @NotNull(message = "La capacidad del lugar es obligatoria")
        @Positive(message = "La capacidad debe ser mayor que cero")
        Integer capacity,

        @NotBlank(message = "La ciudad del lugar es obligatoria")
        @Size(max = 80, message = "El nombre de la ciudad no puede superar los 80 caracteres")
        String city
) {
}
