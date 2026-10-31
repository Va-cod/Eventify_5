package com.eventify_4.controller.web;

import com.eventify_4.dto.request.CategoryRequestDTO;
import com.eventify_4.dto.response.CategoryResponseDTO;
import com.eventify_4.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryWebController {

    private final CategoryService categoryService;

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new CategoryRequestDTO("", ""));
        model.addAttribute("isEdit", false);
        return "categories/form";
    }

    @PostMapping("/new")
    public String createCategory(
            @Valid @ModelAttribute("category") CategoryRequestDTO request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("isEdit", false);
            return "categories/form";
        }

        try {
            categoryService.createCategory(request);
            redirectAttributes.addFlashAttribute("successMessage", "Categoría creada exitosamente.");
            return "redirect:/categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("isEdit", false);
            return "categories/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        CategoryResponseDTO category = categoryService.getCategoryById(id);
        model.addAttribute("id", id);
        model.addAttribute("category", new CategoryRequestDTO(category.name(), category.description()));
        model.addAttribute("isEdit", true);
        return "categories/form";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") CategoryRequestDTO request,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (result.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("isEdit", true);
            return "categories/form";
        }

        try {
            categoryService.updateCategory(id, request);
            redirectAttributes.addFlashAttribute("successMessage", "Categoría actualizada exitosamente.");
            return "redirect:/categories";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("id", id);
            model.addAttribute("isEdit", true);
            return "categories/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Categoría eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "No se pudo eliminar la categoría. Asegúrese de que no tenga eventos vinculados.");
        }
        return "redirect:/categories";
    }
}
