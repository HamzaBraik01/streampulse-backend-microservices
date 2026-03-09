package com.streampulse.videoservice.service;

import com.streampulse.videoservice.dto.VideoDTO;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;

import java.util.List;

/**
 * Service interface for Video business logic.
 */
public interface VideoService {

    List<VideoDTO> findAll();

    VideoDTO findById(Long id);

    VideoDTO create(VideoDTO videoDTO);

    VideoDTO update(Long id, VideoDTO videoDTO);

    void delete(Long id);

    List<VideoDTO> search(VideoType type, VideoCategory category);

    List<VideoDTO> searchByTitle(String title);

    List<VideoDTO> findByType(VideoType type);

    List<VideoDTO> findByCategory(VideoCategory category);
}
