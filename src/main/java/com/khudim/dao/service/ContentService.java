package com.khudim.dao.service;

import com.khudim.dao.entity.Content;
import com.khudim.dao.repository.ContentRepository;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public boolean isPathExist(Path path) {
        return contentRepository.getContentByPath(path.toString()) != null;
    }

    public void save(Content content) {
        contentRepository.save(content);
    }

    public String getVideoPath(long contentId) throws NoSuchFileException {
        String path = contentRepository.getVideoPath(contentId);
        if (StringUtils.isBlank(path) || !Files.exists(Paths.get(path))) {
            throw new NoSuchFileException("No such file " + path);
        }
        return contentRepository.getVideoPath(contentId);
    }

    public byte[] getImage(long contentId) {
        return contentRepository.getImage(contentId);
    }
}
