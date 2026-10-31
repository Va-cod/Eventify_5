package com.eventify_4.mapper;

import com.eventify_4.dto.request.EventCreateDTO;
import com.eventify_4.dto.response.EventResponseDTO;
import com.eventify_4.dto.response.EventSummaryDTO;
import com.eventify_4.entity.Category;
import com.eventify_4.entity.Event;
import com.eventify_4.entity.Venue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(source = "venue.name", target = "venueName")
    @Mapping(source = "venue.city", target = "venueCity")
    @Mapping(source = "venue.capacity", target = "venueCapacity")
    @Mapping(source = "venue.id", target = "venueId")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "category", target = "categoryNames", qualifiedByName = "categoryToCategoryNames")
    EventResponseDTO toResponseDTO(Event event);

    List<EventResponseDTO> toResponseDTOList(List<Event> events);

    @Mapping(source = "name", target = "eventName")
    @Mapping(source = "date", target = "eventDate")
    @Mapping(source = "venue.name", target = "venueName")
    @Mapping(source = "venue.city", target = "city")
    @Mapping(source = "category.name", target = "categoryName")
    EventSummaryDTO toSummaryDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(source = "venueId", target = "venue", qualifiedByName = "venueIdToVenue")
    @Mapping(source = "categoryIds", target = "category", qualifiedByName = "categoryIdsToCategory")
    Event toEntity(EventCreateDTO createDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromDTO(EventCreateDTO createDTO, @MappingTarget Event event);

    @Named("categoryToCategoryNames")
    default List<String> categoryToCategoryNames(Category category) {
        if (category == null || category.getName() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(category.getName());
    }

    @Named("categoryIdsToCategory")
    default Category categoryIdsToCategory(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty() || categoryIds.get(0) == null) {
            return null;
        }
        Category category = new Category();
        category.setId(categoryIds.get(0));
        return category;
    }

    @Named("venueIdToVenue")
    default Venue venueIdToVenue(Long venueId) {
        if (venueId == null) {
            return null;
        }
        Venue venue = new Venue();
        venue.setId(venueId);
        return venue;
    }
}
