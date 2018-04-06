package com.khudim.dao.service;

import com.khudim.dao.entity.Tags;
import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import com.khudim.storage.IFileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.PageRequest.of;

/**
 * Created by Beaver.
 */
@Service
public class VideoService {

    private final VideoRepository videoRepository;
    private final TagsService tagsService;
    private final List<String> fileStorages;

    @Autowired
    public VideoService(VideoRepository videoRepository, TagsService tagsService, List<IFileStorage> fileStorages) {
        this.videoRepository = videoRepository;
        this.tagsService = tagsService;
        this.fileStorages = fileStorages.stream().map(storage -> storage.getStorageType().name()).collect(toList());
    }

    public boolean isRepeated(String name) {
        return videoRepository.findFirstByName(name) != null;
    }

    public Video save(Video video) {
        return videoRepository.save(video);
    }

    public List<Video> findAll(int page, int limit) {
        return videoRepository.findByStorage(of(page, limit), fileStorages);
    }

    public List<Video> findByTag(List<String> tags, int page, int limit) {
        if (tags == null || tags.isEmpty()) {
            return findAll(page, limit);
        }
        Set<Tags> loadedTags = tagsService.findTags(tags);
        if (loadedTags.isEmpty()) {
            return Collections.emptyList();
        }

        return videoRepository.findByVideoTagsAndStorage(loadedTags, of(page, limit), fileStorages);
    }

    public long getCount() {
        return videoRepository.countByStorage(fileStorages);
    }

    public long getCount(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return getCount();
        }
        Set<Tags> loadedTags = tagsService.findTags(tags);
        if (loadedTags.isEmpty()) {
            return 0;
        }
        return videoRepository.countByVideoTagsAndStorage(loadedTags, fileStorages);
    }
}
