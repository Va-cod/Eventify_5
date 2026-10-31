package com.eventify_4.controller.web;

import com.eventify_4.service.CategoryService;
import com.eventify_4.service.EventService;
import com.eventify_4.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeWebController {

    private final EventService eventService;
    private final VenueService venueService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalEvents", eventService.countEvents());
        model.addAttribute("totalVenues", venueService.countVenues());
        model.addAttribute("totalCategories", categoryService.countCategories());
        model.addAttribute("recentEvents", eventService.getAllEvents());
        return "home";
    }
}
