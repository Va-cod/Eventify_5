package com.eventify_4.mapper;

import com.eventify_4.dto.request.CategoryCreateDTO;
import com.eventify_4.dto.request.CategoryRequestDTO;
import com.eventify_4.dto.response.CategoryResponseDTO;
import com.eventify_4.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponseDTO toResponseDTO(Category category);

    List<CategoryResponseDTO> toResponseDTOList(List<Category> categories);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Category toEntity(CategoryCreateDTO createDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    Category toEntity(CategoryRequestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateEntityFromDTO(CategoryCreateDTO createDTO, @MappingTarget Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateEntityFromDTO(CategoryRequestDTO requestDTO, @MappingTarget Category category);
}
