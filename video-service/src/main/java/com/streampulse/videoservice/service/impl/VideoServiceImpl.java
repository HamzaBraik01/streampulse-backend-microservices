package com.streampulse.videoservice.service.impl;

import com.streampulse.videoservice.dto.VideoDTO;
import com.streampulse.videoservice.entity.Video;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import com.streampulse.videoservice.exception.VideoNotFoundException;
import com.streampulse.videoservice.mapper.VideoMapper;
import com.streampulse.videoservice.repository.VideoRepository;
import com.streampulse.videoservice.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of VideoService — encapsulates all video business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VideoServiceImpl implements VideoService {

    private final VideoRepository videoRepository;
    private final VideoMapper videoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<VideoDTO> findAll() {
        log.info("Fetching all videos");
        return videoRepository.findAll()
                .stream()
                .map(videoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public VideoDTO findById(Long id) {
        log.info("Fetching video with id: {}", id);
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with id: " + id));
        return videoMapper.toDTO(video);
    }

    @Override
    public VideoDTO create(VideoDTO videoDTO) {
        log.info("Creating new video: {}", videoDTO.getTitle());
        Video video = videoMapper.toEntity(videoDTO);
        Video savedVideo = videoRepository.save(video);
        return videoMapper.toDTO(savedVideo);
    }

    @Override
    public VideoDTO update(Long id, VideoDTO videoDTO) {
        log.info("Updating video with id: {}", id);
        Video existingVideo = videoRepository.findById(id)
                .orElseThrow(() -> new VideoNotFoundException("Video not found with id: " + id));
        videoMapper.updateEntityFromDTO(videoDTO, existingVideo);
        Video updatedVideo = videoRepository.save(existingVideo);
        return videoMapper.toDTO(updatedVideo);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting video with id: {}", id);
        if (!videoRepository.existsById(id)) {
            throw new VideoNotFoundException("Video not found with id: " + id);
        }
        videoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoDTO> search(VideoType type, VideoCategory category) {
        log.info("Searching videos by type: {} and category: {}", type, category);
        List<Video> videos;
        if (type != null && category != null) {
            videos = videoRepository.findByTypeAndCategory(type, category);
        } else if (type != null) {
            videos = videoRepository.findByType(type);
        } else if (category != null) {
            videos = videoRepository.findByCategory(category);
        } else {
            videos = videoRepository.findAll();
        }
        return videos.stream()
                .map(videoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoDTO> searchByTitle(String title) {
        log.info("Searching videos by title: {}", title);
        return videoRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(videoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoDTO> findByType(VideoType type) {
        log.info("Fetching videos by type: {}", type);
        return videoRepository.findByType(type)
                .stream()
                .map(videoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<VideoDTO> findByCategory(VideoCategory category) {
        log.info("Fetching videos by category: {}", category);
        return videoRepository.findByCategory(category)
                .stream()
                .map(videoMapper::toDTO)
                .collect(Collectors.toList());
    }
}
