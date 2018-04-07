package com.khudim.dao.service;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Tags;
import com.khudim.dao.repository.ContentRepository;
import com.khudim.storage.IFileStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.springframework.data.domain.PageRequest.of;

/**
 * Created by Beaver.
 */
@Service
public class ContentService {

    private final ContentRepository contentRepository;
    private final TagsService tagsService;

    @Autowired
    public ContentService(ContentRepository contentRepository, TagsService tagsService) {
        this.contentRepository = contentRepository;
        this.tagsService = tagsService;
    }

    public Content save(Content content) {
        return contentRepository.save(content);
    }

    public Content getContent(long contentId) throws NoSuchFileException {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new NoSuchFileException("Can't find content with id = " + contentId));
    }

    public byte[] getImage(long contentId) {
        return contentRepository.findImageById(contentId);
    }

    public List<Content> findAll(int page, int limit, List<String> fileStorages) {
        return contentRepository.findByStorageIn(of(page, limit), fileStorages);
    }

    public List<Content> findByTag(List<String> tags, int page, int limit, List<IFileStorage> fileStorages) {
        List<String> storages = getStoragesName(fileStorages);
        if (tags == null || tags.isEmpty()) {
            return findAll(page, limit, storages);
        }
        Set<Tags> loadedTags = tagsService.findTags(tags);
        if (loadedTags.isEmpty()) {
            return Collections.emptyList();
        }
        return contentRepository.findByContentTagsInAndStorageIn(loadedTags, of(page, limit), storages);
    }

    public long getCount(List<String> fileStorages) {
        return contentRepository.countByStorageIn(fileStorages);
    }

    public long getCount(List<String> tags, List<IFileStorage> fileStorages) {
        List<String> storages = getStoragesName(fileStorages);
        if (tags == null || tags.isEmpty()) {
            return getCount(storages);
        }
        Set<Tags> contentTags = tagsService.findTags(tags);
        if (contentTags.isEmpty()) {
            return 0;
        }
        return contentRepository.countByContentTagsInAndStorageIn(contentTags, storages);
    }

    private List<String> getStoragesName(List<IFileStorage> fileStorages) {
        return fileStorages.stream().map(s -> s.getStorageType().name()).collect(toList());
    }
}
