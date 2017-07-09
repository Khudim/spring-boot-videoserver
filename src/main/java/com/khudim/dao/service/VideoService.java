package com.khudim.dao.service;

import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Beaver.
 */
@Service
public class VideoService {

    private final VideoRepository videoRepository;

    @Autowired
    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    public boolean isRepeated(String name) {
        return videoRepository.countByName(name) > 0;
    }

    public void save(Video video) {
        videoRepository.save(video);
    }
}
