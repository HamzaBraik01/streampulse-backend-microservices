package com.streampulse.videoservice.service;

import com.streampulse.videoservice.dto.VideoDTO;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;

import java.util.List;

public interface VideoService {

    List<VideoDTO> findAll();

    VideoDTO findById(Long id);

    VideoDTO create(VideoDTO videoDTO);

    VideoDTO update(Long id, VideoDTO videoDTO);

    void delete(Long id);

    List<VideoDTO> search(VideoType type, VideoCategory category);
}
