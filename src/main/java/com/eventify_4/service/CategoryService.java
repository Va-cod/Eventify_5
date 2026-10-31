package com.eventify_4.service;

import com.eventify_4.dto.request.CategoryCreateDTO;
import com.eventify_4.dto.request.CategoryRequestDTO;
import com.eventify_4.dto.response.CategoryResponseDTO;
import com.eventify_4.entity.Category;
import com.eventify_4.exception.InvalidCategoryException;
import com.eventify_4.mapper.CategoryMapper;
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
    private final CategoryMapper categoryMapper;

    public List<CategoryResponseDTO> getAllCategories() {
        return categoryMapper.toResponseDTOList(categoryRepository.findAll());
    }

    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new InvalidCategoryException("Categoría no encontrada con id: " + id));
        return categoryMapper.toResponseDTO(category);
    }

    public long countCategories() {
        return categoryRepository.count();
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateDTO request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new InvalidCategoryException("Ya existe una categoría con el nombre: " + request.name());
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(savedCategory);
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        return createCategory(new CategoryCreateDTO(request.name(), request.description()));
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryCreateDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new InvalidCategoryException("Categoría no encontrada con id: " + id));

        if (!category.getName().equalsIgnoreCase(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new InvalidCategoryException("Ya existe otra categoría con el nombre: " + request.name());
        }

        categoryMapper.updateEntityFromDTO(request, category);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(updatedCategory);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        return updateCategory(id, new CategoryCreateDTO(request.name(), request.description()));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new InvalidCategoryException("Categoría no encontrada con id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
