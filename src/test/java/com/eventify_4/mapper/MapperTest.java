package com.eventify_4.mapper;

import com.eventify_4.dto.request.CategoryCreateDTO;
import com.eventify_4.dto.request.EventCreateDTO;
import com.eventify_4.dto.request.VenueCreateDTO;
import com.eventify_4.dto.response.CategoryResponseDTO;
import com.eventify_4.dto.response.EventResponseDTO;
import com.eventify_4.dto.response.EventSummaryDTO;
import com.eventify_4.dto.response.VenueResponseDTO;
import com.eventify_4.entity.Category;
import com.eventify_4.entity.Event;
import com.eventify_4.entity.Venue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTest {

    private final EventMapper eventMapper = Mappers.getMapper(EventMapper.class);
    private final VenueMapper venueMapper = Mappers.getMapper(VenueMapper.class);
    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

    @Test
    @DisplayName("Debe mapear Event a EventResponseDTO correctamente con nombres personalizados")
    void shouldMapEventToEventResponseDTO() {
        Venue venue = Venue.builder()
                .id(10L)
                .name("Movistar Arena")
                .city("Bogotá")
                .capacity(14000)
                .build();

        Category category = Category.builder()
                .id(5L)
                .name("Música")
                .build();

        Event event = Event.builder()
                .id(1L)
                .name("Concierto Sinfónico")
                .date(LocalDate.of(2026, 11, 20))
                .description("Gran concierto en vivo")
                .venue(venue)
                .category(category)
                .active(true)
                .build();

        EventResponseDTO response = eventMapper.toResponseDTO(event);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Concierto Sinfónico", response.name());
        assertEquals(LocalDate.of(2026, 11, 20), response.date());
        assertEquals("Gran concierto en vivo", response.description());
        assertEquals("Movistar Arena", response.venueName());
        assertEquals(List.of("Música"), response.categoryNames());
        assertEquals("Bogotá", response.venueCity());
        assertEquals(14000, response.venueCapacity());
        assertEquals(10L, response.venueId());
        assertEquals(5L, response.categoryId());
        assertEquals("Música", response.categoryName());
        assertTrue(response.active());
    }

    @Test
    @DisplayName("Debe mapear Event a EventSummaryDTO manteniendo la optimización de proyecciones")
    void shouldMapEventToEventSummaryDTO() {
        Venue venue = Venue.builder().id(2L).name("Teatro Mayor").city("Bogotá").build();
        Category category = Category.builder().id(3L).name("Teatro").build();

        Event event = Event.builder()
                .id(100L)
                .name("Festival de Danza")
                .date(LocalDate.of(2026, 12, 1))
                .venue(venue)
                .category(category)
                .build();

        EventSummaryDTO summary = eventMapper.toSummaryDTO(event);

        assertNotNull(summary);
        assertEquals(100L, summary.id());
        assertEquals("Festival de Danza", summary.eventName());
        assertEquals(LocalDate.of(2026, 12, 1), summary.eventDate());
        assertEquals("Teatro Mayor", summary.venueName());
        assertEquals("Bogotá", summary.city());
        assertEquals("Teatro", summary.categoryName());
    }

    @Test
    @DisplayName("Debe mapear EventCreateDTO a la entidad Event con referencias por ID y estado activo")
    void shouldMapEventCreateDTOToEntity() {
        EventCreateDTO createDTO = new EventCreateDTO(
                "Hackathon IA",
                LocalDate.of(2026, 11, 25),
                "Desarrollo de agentes inteligentes",
                2L,
                List.of(4L)
        );

        Event event = eventMapper.toEntity(createDTO);

        assertNotNull(event);
        assertNull(event.getId());
        assertEquals("Hackathon IA", event.getName());
        assertEquals(LocalDate.of(2026, 11, 25), event.getDate());
        assertEquals("Desarrollo de agentes inteligentes", event.getDescription());
        assertTrue(event.getActive());
        assertNotNull(event.getVenue());
        assertEquals(2L, event.getVenue().getId());
        assertNotNull(event.getCategory());
        assertEquals(4L, event.getCategory().getId());
    }

    @Test
    @DisplayName("Debe mapear VenueCreateDTO a Venue y Venue a VenueResponseDTO")
    void shouldMapVenueCorrectly() {
        VenueCreateDTO createDTO = new VenueCreateDTO(
                "Centro de Convenciones",
                "Carrera 7 # 100",
                3500,
                "Cali"
        );

        Venue venue = venueMapper.toEntity(createDTO);
        assertNotNull(venue);
        assertNull(venue.getId());
        assertEquals("Centro de Convenciones", venue.getName());
        assertEquals("Carrera 7 # 100", venue.getAddress());
        assertEquals(3500, venue.getCapacity());
        assertEquals("Cali", venue.getCity());
        assertTrue(venue.isStatus());

        venue.setId(50L);
        VenueResponseDTO response = venueMapper.toResponseDTO(venue);
        assertNotNull(response);
        assertEquals(50L, response.id());
        assertEquals("Centro de Convenciones", response.name());
        assertEquals("Carrera 7 # 100", response.address());
        assertEquals(3500, response.capacity());
        assertEquals("Cali", response.city());
    }

    @Test
    @DisplayName("Debe mapear CategoryCreateDTO a Category y Category a CategoryResponseDTO")
    void shouldMapCategoryCorrectly() {
        CategoryCreateDTO createDTO = new CategoryCreateDTO(
                "Gastronomía",
                "Eventos culinarios y ferias gastronómicas"
        );

        Category category = categoryMapper.toEntity(createDTO);
        assertNotNull(category);
        assertNull(category.getId());
        assertEquals("Gastronomía", category.getName());
        assertEquals("Eventos culinarios y ferias gastronómicas", category.getDescription());

        category.setId(9L);
        CategoryResponseDTO response = categoryMapper.toResponseDTO(category);
        assertNotNull(response);
        assertEquals(9L, response.id());
        assertEquals("Gastronomía", response.name());
        assertEquals("Eventos culinarios y ferias gastronómicas", response.description());
    }
}
