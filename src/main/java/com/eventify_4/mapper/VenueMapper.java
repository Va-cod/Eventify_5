package com.eventify_4.mapper;

import com.eventify_4.dto.request.VenueCreateDTO;
import com.eventify_4.dto.request.VenueResquestDTO;
import com.eventify_4.dto.response.VenueResponseDTO;
import com.eventify_4.entity.Venue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VenueMapper {

    VenueResponseDTO toResponseDTO(Venue venue);

    List<VenueResponseDTO> toResponseDTOList(List<Venue> venues);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "true")
    @Mapping(target = "events", ignore = true)
    Venue toEntity(VenueCreateDTO createDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "true")
    @Mapping(target = "events", ignore = true)
    Venue toEntity(VenueResquestDTO requestDTO);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateEntityFromDTO(VenueCreateDTO createDTO, @MappingTarget Venue venue);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "events", ignore = true)
    void updateEntityFromDTO(VenueResquestDTO requestDTO, @MappingTarget Venue venue);
}
