package com.eventify_4.repository;

import com.eventify_4.dto.response.EventSummaryDTO;
import com.eventify_4.entity.Event;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    // 1. Filtrar por Ciudad del lugar con ordenamiento cronológico descendente
    @EntityGraph(attributePaths = {"venue", "category"})
    Slice<Event> findByVenue_CityIgnoreCaseOrderByDateDesc(String city, Pageable pageable);

    // 2. Filtrar por Nombre de Categoría con ordenamiento descendente
    @EntityGraph(attributePaths = {"venue", "category"})
    Slice<Event> findByCategory_NameIgnoreCaseOrderByDateDesc(String categoryName, Pageable pageable);

    // 3. Filtrar por Rango de Fechas con ordenamiento descendente
    @EntityGraph(attributePaths = {"venue", "category"})
    Slice<Event> findByDateBetweenOrderByDateDesc(LocalDate startDate, LocalDate endDate, Pageable pageable);

    // 4. Cargar lista completa en una sola consulta
    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.venue
        JOIN FETCH e.category
    """)
    List<Event> findAllWithVenueAndCategory();

    // 5. Compatible con paginación optimizada (Slice)
    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.venue
        JOIN FETCH e.category
    """)
    Slice<Event> findAllWithVenueAndCategory(Pageable pageable);

    // 6. Buscar por ID garantizando una sola consulta
    @Query("""
        SELECT e FROM Event e
        JOIN FETCH e.venue
        JOIN FETCH e.category
        WHERE e.id = :id
    """)
    Optional<Event> findByIdWithDetails(@Param("id") Long id);

    // 7. Verificar disponibilidad del recinto para una fecha específica
    boolean existsByVenue_IdAndDate(Long venueId, LocalDate date);
}
