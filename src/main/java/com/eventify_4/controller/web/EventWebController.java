package com.eventify_4.controller.web;

import com.eventify_4.dto.request.EventCreateDTO;
import com.eventify_4.dto.response.EventResponseDTO;
import com.eventify_4.service.CategoryService;
import com.eventify_4.service.EventService;
import com.eventify_4.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventWebController {

    private final EventService eventService;
    private final VenueService venueService;
    private final CategoryService categoryService;

    @GetMapping
    public String listEvents(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        try {
            List<EventResponseDTO> events = eventService.searchEvents(city, category, startDate, endDate);
            model.addAttribute("events", events);
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("events", eventService.getAllEvents());
        }

        model.addAttribute("selectedCity", city);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("venues", venueService.getAllVenues());
        model.addAttribute("categories", categoryService.getAllCategories());

        return "events/list";
    }

    @GetMapping("/{id}")
    public String showEventDetail(@PathVariable Long id, Model model) {
        EventResponseDTO event = eventService.getEventResponseById(id);
        model.addAttribute("event", event);
        return "events/detail";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("event", new EventCreateDTO("", null, "", null, (Long) null));
        model.addAttribute("venues", venueService.getAllVenues());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", false);
        return "events/form";
    }

    @PostMapping("/new")
    public String createEvent(
            @Valid @ModelAttribute("event") EventCreateDTO request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("venues", venueService.getAllVenues());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", false);
            return "events/form";
        }

        try {
            eventService.createEvent(request);
            redirectAttributes.addFlashAttribute("successMessage", "Evento programado exitosamente.");
            return "redirect:/events";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("venues", venueService.getAllVenues());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", false);
            return "events/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        EventResponseDTO event = eventService.getEventResponseById(id);
        model.addAttribute("id", id);
        model.addAttribute("event", new EventCreateDTO(
                event.name(),
                event.date(),
                event.description(),
                event.venueId(),
                event.categoryId()
        ));
        model.addAttribute("venues", venueService.getAllVenues());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEdit", true);
        return "events/form";
    }

    @PostMapping("/edit/{id}")
    public String updateEvent(
            @PathVariable Long id,
            @Valid @ModelAttribute("event") EventCreateDTO request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("venues", venueService.getAllVenues());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", true);
            return "events/form";
        }

        try {
            eventService.updateEvent(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Evento actualizado exitosamente.");
            return "redirect:/events";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("id", id);
            model.addAttribute("venues", venueService.getAllVenues());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("isEdit", true);
            return "events/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Evento eliminado (desactivado) exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el evento: " + e.getMessage());
        }
        return "redirect:/events";
    }
}
