package com.khudim.dao.service;

import com.khudim.dao.entity.Tags;
import com.khudim.dao.repository.TagsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

@Service
public class TagsService {

    private final TagsRepository tagsRepository;

    @Autowired
    public TagsService(TagsRepository tagsRepository) {
        this.tagsRepository = tagsRepository;
    }

    public List<String> findTop10() {
        return tagsRepository.findTop10ByOrderByCount()
                .stream()
                .map(Tags::getTag)
                .collect(Collectors.toList());
    }

    public Set<Tags> findTags(List<String> tags) {
        return tags.stream().map(tag -> {
            Tags existedTag = tagsRepository.findFirstByTag(tag);
            if (existedTag == null) {
                existedTag = tagsRepository.save(new Tags(tag));
            } else {
                existedTag.incrementCount();
            }
            return existedTag;
        }).collect(toSet());
    }

    public void save(Tags tag) {
        tagsRepository.save(tag);
    }
}
