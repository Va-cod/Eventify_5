package com.eventify_4.controller;

import com.eventify_4.entity.Category;
import com.eventify_4.entity.Event;
import com.eventify_4.entity.Venue;
import com.eventify_4.repository.CategoryRepository;
import com.eventify_4.repository.EventRepository;
import com.eventify_4.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class EventRestControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EventRepository eventRepository;

    private MockMvc mockMvc;
    private Venue testVenue;
    private Category testCategory;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        testVenue = venueRepository.save(Venue.builder()
                .name("Movistar Arena Test")
                .address("Diagonal 61C # 26-36")
                .capacity(14000)
                .city("Bogotá")
                .status(true)
                .build());

        testCategory = categoryRepository.save(Category.builder()
                .name("Tecnología Test " + System.currentTimeMillis())
                .description("Categoría de prueba")
                .build());

        testEvent = eventRepository.save(Event.builder()
                .name("Concierto Sinfónico Test")
                .date(LocalDate.now().plusDays(10))
                .description("Evento musical de prueba")
                .venue(testVenue)
                .category(testCategory)
                .active(true)
                .build());
    }

    @Test
    @DisplayName("GET /api/events debe retornar un Slice de EventSummaryDTO manteniendo la optimización de Semana 4")
    void shouldReturnSliceOfEventSummaryDTO() throws Exception {
        mockMvc.perform(get("/api/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.content[0].eventName", notNullValue()))
                .andExpect(jsonPath("$.content[0].eventDate", notNullValue()))
                .andExpect(jsonPath("$.content[0].venueName", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/events/{id} debe retornar EventResponseDTO sin exponer entidades JPA internas")
    void shouldReturnEventResponseDTOById() throws Exception {
        mockMvc.perform(get("/api/events/" + testEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId()))
                .andExpect(jsonPath("$.name").value("Concierto Sinfónico Test"))
                .andExpect(jsonPath("$.venueName").value(testVenue.getName()))
                .andExpect(jsonPath("$.categoryNames", hasItem(testCategory.getName())));
    }

    @Test
    @DisplayName("POST /api/events con EventCreateDTO debe crear y responder con EventResponseDTO mapeado con MapStruct")
    void shouldCreateEventWithEventCreateDTO() throws Exception {
        String jsonPayload = String.format("""
                {
                    "name": "Conferencia MapStruct 2026",
                    "date": "%s",
                    "description": "Sesión técnica sobre arquitectura DTO y mappers automatizados",
                    "venueId": %d,
                    "categoryIds": [%d]
                }
                """, LocalDate.now().plusDays(20), testVenue.getId(), testCategory.getId());

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Conferencia MapStruct 2026"))
                .andExpect(jsonPath("$.venueName").value(testVenue.getName()))
                .andExpect(jsonPath("$.categoryNames", hasItem(testCategory.getName())));
    }

    @Test
    @DisplayName("POST /api/venues con VenueCreateDTO debe crear un nuevo recinto exitosamente")
    void shouldCreateVenueWithVenueCreateDTO() throws Exception {
        String jsonPayload = """
                {
                    "name": "Auditorio Principal Tech",
                    "address": "Calle 100 # 15-20",
                    "capacity": 2500,
                    "city": "Bogotá"
                }
                """;

        mockMvc.perform(post("/api/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Auditorio Principal Tech"))
                .andExpect(jsonPath("$.capacity").value(2500));
    }
}
