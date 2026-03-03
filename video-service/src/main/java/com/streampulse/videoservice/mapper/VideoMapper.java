package com.streampulse.videoservice.mapper;

import com.streampulse.videoservice.dto.VideoDTO;
import com.streampulse.videoservice.entity.Video;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VideoMapper {

    /**
     * Convert a Video entity to a VideoDTO.
     */
    VideoDTO toDTO(Video video);

    /**
     * Convert a VideoDTO to a Video entity.
     */
    Video toEntity(VideoDTO videoDTO);

    /**
     * Update an existing Video entity from a VideoDTO.
     * The target entity fields are updated in place.
     */
    void updateEntityFromDTO(VideoDTO videoDTO, @MappingTarget Video video);
}
