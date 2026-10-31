package com.eventify_4.controller;

import com.eventify_4.dto.request.VenueCreateDTO;
import com.eventify_4.dto.response.VenueResponseDTO;
import com.eventify_4.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
@Tag(name = "Lugares / Recintos", description = "Endpoints para la gestión de recintos y espacios para eventos")
public class VenueController {

    private final VenueService venueService;

    @Operation(summary = "Listar todos los lugares disponibles")
    @ApiResponse(responseCode = "200", description = "Listado de recintos recuperado exitosamente")
    @GetMapping
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues() {
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @Operation(summary = "Obtener el detalle de un lugar por su ID")
    @ApiResponse(responseCode = "200", description = "Lugar encontrado exitosamente")
    @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    @GetMapping("/{id}")
    public ResponseEntity<VenueResponseDTO> getVenueById(@PathVariable Long id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @Operation(summary = "Crear y registrar un nuevo lugar")
    @ApiResponse(responseCode = "201", description = "Lugar creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Capacidad inválida o datos de entrada incorrectos")
    @PostMapping
    public ResponseEntity<VenueResponseDTO> createVenue(@Valid @RequestBody VenueCreateDTO request) {
        VenueResponseDTO created = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar información de un lugar existente")
    @ApiResponse(responseCode = "200", description = "Lugar actualizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    @PutMapping("/{id}")
    public ResponseEntity<VenueResponseDTO> updateVenue(
            @PathVariable Long id,
            @Valid @RequestBody VenueCreateDTO request
    ) {
        return ResponseEntity.ok(venueService.updateVenue(id, request));
    }

    @Operation(summary = "Eliminar un lugar por su ID")
    @ApiResponse(responseCode = "204", description = "Lugar eliminado exitosamente")
    @ApiResponse(responseCode = "404", description = "Lugar no encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        venueService.deleteVenue(id);
        return ResponseEntity.noContent().build();
    }
}
