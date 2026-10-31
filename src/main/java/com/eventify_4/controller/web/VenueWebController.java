package com.eventify_4.controller.web;

import com.eventify_4.dto.request.VenueCreateDTO;
import com.eventify_4.dto.response.VenueResponseDTO;
import com.eventify_4.service.VenueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueWebController {

    private final VenueService venueService;

    @GetMapping
    public String listVenues(Model model) {
        model.addAttribute("venues", venueService.getAllVenues());
        return "venues/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("venue", new VenueCreateDTO("", "", null, ""));
        model.addAttribute("isEdit", false);
        return "venues/form";
    }

    @PostMapping("/new")
    public String createVenue(
            @Valid @ModelAttribute("venue") VenueCreateDTO request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "venues/form";
        }

        try {
            venueService.createVenue(request);
            redirectAttributes.addFlashAttribute("successMessage", "Lugar registrado exitosamente.");
            return "redirect:/venues";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", false);
            return "venues/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        VenueResponseDTO venue = venueService.getVenueById(id);
        model.addAttribute("id", id);
        model.addAttribute("venue", new VenueCreateDTO(
                venue.name(),
                venue.address(),
                venue.capacity(),
                venue.city()
        ));
        model.addAttribute("isEdit", true);
        return "venues/form";
    }

    @PostMapping("/edit/{id}")
    public String updateVenue(
            @PathVariable Long id,
            @Valid @ModelAttribute("venue") VenueCreateDTO request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("isEdit", true);
            return "venues/form";
        }

        try {
            venueService.updateVenue(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Lugar actualizado exitosamente.");
            return "redirect:/venues";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("id", id);
            model.addAttribute("isEdit", true);
            return "venues/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteVenue(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            venueService.deleteVenue(id);
            redirectAttributes.addFlashAttribute("successMessage", "Lugar eliminado exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error al eliminar el lugar: " + e.getMessage());
        }
        return "redirect:/venues";
    }
}
