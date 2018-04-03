package com.khudim.dao.service;

import com.khudim.dao.entity.Tags;
import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.springframework.data.domain.PageRequest.of;

/**
 * Created by Beaver.
 */
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final TagsService tagsService;

    @Autowired
    public VideoService(VideoRepository videoRepository, TagsService tagsService) {
        this.videoRepository = videoRepository;
        this.tagsService = tagsService;
    }

    public boolean isRepeated(String name) {
        return videoRepository.findFirstByName(name) != null;
    }

    public void save(Video video) {
        videoRepository.save(video);
    }

    public List<Video> findAll(int page, int limit) {
        return videoRepository.findAll(of(page, limit)).getContent();
    }

    public List<Video> findByTag(List<String> tags, int page, int limit) {
        if (tags == null || tags.isEmpty()) {
            return findAll(page, limit);
        }
        Set<Tags> loadedTags = tagsService.findOrCreateTags(tags);
        if (loadedTags.isEmpty()) {
            return Collections.emptyList();
        }

        return videoRepository.findByVideoTags(loadedTags, of(page, limit));
    }

    public long getCount() {
        return videoRepository.count();
    }

    public long getCount(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return getCount();
        }
        Set<Tags> loadedTags = tagsService.findOrCreateTags(tags);
        if (loadedTags.isEmpty()) {
            return 0;
        }
        return videoRepository.countByVideoTags(loadedTags);
    }
}
