package com.khudim.dao.service;

import com.khudim.dao.entity.Content;
import com.khudim.dao.repository.ContentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.NoSuchFileException;

/**
 * Created by Beaver.
 */
@Service
public class ContentService {

    private final ContentRepository contentRepository;

    @Autowired
    public ContentService(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void save(Content content) {
        contentRepository.save(content);
    }

    public Content getContent(long contentId) throws NoSuchFileException {
        return contentRepository.findById(contentId)
                .orElseThrow(() -> new NoSuchFileException("Can't find content with id = " + contentId));
    }

    public byte[] getImage(long contentId) {
        return contentRepository.findImageById(contentId);
    }
}
