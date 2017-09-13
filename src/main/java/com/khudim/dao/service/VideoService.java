package com.khudim.dao.service;

import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        List<Video> videos = videoRepository.findAll(new PageRequest(page, limit)).getContent();
        if (videos == null) {
            videos = Collections.emptyList();
        }
        return videos;
    }

    public List<Video> findAllByTags(List<String> tags, int page, int limit) {
        if (tags.isEmpty()) {
            return findAll(page, limit);
        }
        return videoRepository.findAllByTags(tags, page * limit, limit).stream().limit(limit).skip(page * limit).filter(r -> {
            System.out.println(r);
            return true;
        }).collect(Collectors.toList());

    }

    public long getCount() {
        return videoRepository.count();
    }
}
