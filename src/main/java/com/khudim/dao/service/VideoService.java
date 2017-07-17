package com.khudim.dao.service;

import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

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
        if (videoRepository.countByName(name) > 0) {
            System.out.println(name);
        }
        return videoRepository.countByName(name) > 0;
    }

    public void save(Video video) {
        videoRepository.save(video);
    }

    public List<Video> findAll(int page, int limit) {
        return videoRepository.findAll(new PageRequest(page, limit)).getContent();
    }

    public long getCount() {
        return videoRepository.count();
    }
}
