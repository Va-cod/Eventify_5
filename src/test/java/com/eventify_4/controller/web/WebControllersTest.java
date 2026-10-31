package com.eventify_4.controller.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
class WebControllersTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @DisplayName("Debe cargar la página principal de inicio (Dashboard)")
    void shouldLoadHomePage() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Eventify")))
                .andExpect(content().string(containsString("Plataforma de Gestión de Eventos")));
    }

    @Test
    @DisplayName("Debe cargar la vista de lista de categorías")
    void shouldLoadCategoriesListPage() throws Exception {
        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/list"))
                .andExpect(content().string(containsString("Gestión de Categorías")));
    }

    @Test
    @DisplayName("Debe cargar el formulario para crear nueva categoría")
    void shouldLoadNewCategoryForm() throws Exception {
        mockMvc.perform(get("/categories/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"))
                .andExpect(content().string(containsString("Nueva Categoría")));
    }

    @Test
    @DisplayName("Debe validar y rechazar categoría con campos vacíos")
    void shouldRejectEmptyCategoryForm() throws Exception {
        mockMvc.perform(post("/categories/new")
                        .param("name", "")
                        .param("description", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("Debe crear una nueva categoría exitosamente y redirigir")
    void shouldCreateCategorySuccessfully() throws Exception {
        mockMvc.perform(post("/categories/new")
                        .param("name", "Categoría Test Única")
                        .param("description", "Descripción de prueba para test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Debe cargar la vista de lista de lugares (venues)")
    void shouldLoadVenuesListPage() throws Exception {
        mockMvc.perform(get("/venues"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/list"))
                .andExpect(content().string(containsString("Lugares y Recintos")));
    }

    @Test
    @DisplayName("Debe cargar el formulario para crear nuevo lugar")
    void shouldLoadNewVenueForm() throws Exception {
        mockMvc.perform(get("/venues/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/form"))
                .andExpect(content().string(containsString("Registrar Nuevo Lugar")));
    }

    @Test
    @DisplayName("Debe validar y rechazar lugar con capacidad inválida")
    void shouldRejectInvalidVenueCapacity() throws Exception {
        mockMvc.perform(post("/venues/new")
                        .param("name", "Teatro Test")
                        .param("address", "Calle 100")
                        .param("capacity", "-10")
                        .param("city", "Bogotá"))
                .andExpect(status().isOk())
                .andExpect(view().name("venues/form"))
                .andExpect(model().hasErrors());
    }

    @Test
    @DisplayName("Debe crear un nuevo lugar exitosamente y redirigir")
    void shouldCreateVenueSuccessfully() throws Exception {
        mockMvc.perform(post("/venues/new")
                        .param("name", "Lugar Test Único")
                        .param("address", "Avenida Siempre Viva 123")
                        .param("capacity", "5000")
                        .param("city", "Medellín"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/venues"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Debe cargar la vista de lista de eventos con filtros")
    void shouldLoadEventsListPage() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/list"))
                .andExpect(content().string(containsString("Gestión de Eventos")));
    }

    @Test
    @DisplayName("Debe cargar el formulario para programar nuevo evento")
    void shouldLoadNewEventForm() throws Exception {
        mockMvc.perform(get("/events/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(content().string(containsString("Programar Nuevo Evento")));
    }

    @Test
    @DisplayName("Debe rechazar evento con fecha pasada")
    void shouldRejectEventWithPastDate() throws Exception {
        mockMvc.perform(post("/events/new")
                        .param("name", "Evento Pasado")
                        .param("date", "2020-01-01")
                        .param("description", "Descripción")
                        .param("venueId", "1")
                        .param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("events/form"))
                .andExpect(model().hasErrors());
    }
}
