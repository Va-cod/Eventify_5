package com.eventify_4.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryCreateDTO(
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String name,

        @NotBlank(message = "La descripción de la categoría es obligatoria")
        @Size(max = 150, message = "La descripción no puede superar los 150 caracteres")
        String description
) {
}
