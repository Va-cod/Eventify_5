package com.eventify_4.service;

import com.eventify_4.dto.request.CategoryRequestDTO;
import com.eventify_4.dto.response.CategoryResponseDTO;
import com.eventify_4.entity.Category;
import com.eventify_4.exception.InvalidCategoryException;
import com.eventify_4.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new InvalidCategoryException("Categoría no encontrada con id: " + id));
        return mapToResponseDTO(category);
    }

    public long countCategories() {
        return categoryRepository.count();
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new InvalidCategoryException("Ya existe una categoría con el nombre: " + request.name());
        }

        Category category = Category.builder()
                .name(request.name())
                .description(request.description())
                .build();

        Category savedCategory = categoryRepository.save(category);
        return mapToResponseDTO(savedCategory);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new InvalidCategoryException("Categoría no encontrada con id: " + id));

        if (!category.getName().equalsIgnoreCase(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new InvalidCategoryException("Ya existe otra categoría con el nombre: " + request.name());
        }

        category.setName(request.name());
        category.setDescription(request.description());

        Category updatedCategory = categoryRepository.save(category);
        return mapToResponseDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new InvalidCategoryException("Categoría no encontrada con id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponseDTO mapToResponseDTO(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription()
        );
    }
}
