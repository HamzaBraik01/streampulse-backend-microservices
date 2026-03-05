package com.streampulse.videoservice.repository;

import com.streampulse.videoservice.entity.Video;
import com.streampulse.videoservice.entity.VideoCategory;
import com.streampulse.videoservice.entity.VideoType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    List<Video> findByType(VideoType type);

    List<Video> findByCategory(VideoCategory category);

    List<Video> findByTypeAndCategory(VideoType type, VideoCategory category);
}
