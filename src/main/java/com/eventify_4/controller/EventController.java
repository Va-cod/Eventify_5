package com.eventify_4.controller;

import com.eventify_4.dto.request.EventCreateDTO;
import com.eventify_4.dto.response.EventResponseDTO;
import com.eventify_4.dto.response.EventSummaryDTO;
import com.eventify_4.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Eventos", description = "Endpoints para la gestión y consulta optimizada de eventos públicos")
public class EventController {

    private final EventService eventService;

    @Operation(
            summary = "Listar eventos activos con paginación optimizada (Slice) y filtros opcionales",
            description = """
                Recupera un fragmento (`Slice`) de eventos activos ordenados cronológicamente de forma descendente.
                
                **Reglas de negocio y optimizaciones aplicadas:**
                * **Paginación con Slice:** Evita la ejecución del conteo total (`COUNT(*)`), reduciendo la latencia de la base de datos.
                * **Resolución N+1:** Las relaciones con Lugar (`Venue`) y Categoría (`Category`) son cargadas en una sola operación SQL.
                * **Borrado Lógico (Soft Delete):** Solo se entregan eventos con estado activo (`active = true`).
                * **Ordenamiento Garantizado:** Los listados se entregan con orden cronológico descendente (`OrderByDateDesc`).
                """
    )
    @ApiResponse(responseCode = "200", description = "Fragmento de eventos recuperado exitosamente")
    @GetMapping
    public ResponseEntity<Slice<EventSummaryDTO>> getEvents(
            @Parameter(description = "Filtrar por ciudad del lugar del evento", example = "Bogotá")
            @RequestParam(required = false) String city,

            @Parameter(description = "Filtrar por nombre de la categoría del evento", example = "Música")
            @RequestParam(required = false) String category,

            @Parameter(description = "Fecha inicial del rango de búsqueda (ISO-8601)", example = "2026-11-01")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "Fecha final del rango de búsqueda (ISO-8601)", example = "2026-12-31")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,

            @Parameter(hidden = true)
            @PageableDefault(size = 10, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        if (city != null && !city.isBlank()) {
            return ResponseEntity.ok(eventService.searchByCity(city, pageable));
        }

        if (category != null && !category.isBlank()) {
            return ResponseEntity.ok(eventService.searchByCategory(category, pageable));
        }

        if (startDate != null || endDate != null) {
            return ResponseEntity.ok(eventService.searchByDateBetween(startDate, endDate, pageable));
        }

        return ResponseEntity.ok(eventService.getEventsSlice(pageable));
    }

    @Operation(summary = "Obtener el detalle de un evento por su ID")
    @ApiResponse(responseCode = "200", description = "Evento encontrado exitosamente")
    @ApiResponse(responseCode = "404", description = "Evento no encontrado o marcado como inactivo")
    @GetMapping("/{id}")
    public ResponseEntity<EventResponseDTO> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEventById(id));
    }

    @Operation(
            summary = "Crear un nuevo evento",
            description = """
                Registra un evento validando que:
                * La fecha sea presente o futura.
                * El lugar (`Venue`) exista y no esté reservado para otro evento en la misma fecha (`VenueAlreadyBookedException`).
                * La categoría (`Category`) exista.
                """
    )
    @ApiResponse(responseCode = "201", description = "Evento creado exitosamente")
    @ApiResponse(responseCode = "400", description = "Fecha inválida o campos requeridos ausentes")
    @ApiResponse(responseCode = "404", description = "Lugar o categoría no encontrados")
    @ApiResponse(responseCode = "409", description = "El lugar ya está reservado para la fecha indicada")
    @PostMapping
    public ResponseEntity<EventResponseDTO> createEvent(@Valid @RequestBody EventCreateDTO request) {
        EventResponseDTO created = eventService.createEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Actualizar los datos de un evento existente")
    @ApiResponse(responseCode = "200", description = "Evento actualizado exitosamente")
    @ApiResponse(responseCode = "404", description = "Evento, lugar o categoría no encontrados")
    @ApiResponse(responseCode = "409", description = "El lugar seleccionado ya está ocupado en la nueva fecha")
    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDTO> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventCreateDTO request
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, request));
    }

    @Operation(
            summary = "Eliminar un evento (Borrado Lógico / Soft Delete)",
            description = "Aplica una eliminación lógica transformando la operación en un UPDATE events SET active = false WHERE id = ?"
    )
    @ApiResponse(responseCode = "204", description = "Evento desactivado exitosamente")
    @ApiResponse(responseCode = "404", description = "Evento no encontrado")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }
}
