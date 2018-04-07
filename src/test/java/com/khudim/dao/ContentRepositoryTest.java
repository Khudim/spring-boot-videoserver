package com.khudim.dao;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Tags;
import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.ContentRepository;
import com.khudim.dao.repository.TagsRepository;
import com.khudim.dao.repository.VideoRepository;
import com.khudim.storage.StorageType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.domain.PageRequest.of;

/**
 * Created by Beaver.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
public class ContentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private TagsRepository tagsRepository;

    @Autowired
    private VideoRepository videoRepository;

    private String path = "path";

    @Before
    public void setup() {
        Tags tags1 = new Tags(".webm");
        Tags tags2 = new Tags(".mus");
        Content content1 = new Content();
        content1.setContentTags(Set.of(tags1));
        content1.setPath("path");
        content1.setStorage(StorageType.LOCAL_STORAGE.name());
        Content content2 = new Content();
        content2.setContentTags(Set.of(tags1));
        content2.setPath("path2");
        content2.setStorage(StorageType.LOCAL_STORAGE.name());
        Content content3 = new Content();
        content3.setContentTags(Set.of(tags2));
        content3.setStorage(StorageType.LOCAL_STORAGE.name());
        Set<Content> contents = new HashSet<>();
        contents.add(content1);
        contents.add(content2);
        tags1.setContents(contents);
        entityManager.persist(content1);
        entityManager.persist(content2);
        entityManager.persist(content3);
        Content expectedContent = new Content();
        expectedContent.setPath(path);
        entityManager.persist(expectedContent);
    }

    @Test
    public void shouldFindByPath() {
        Long count = contentRepository.countByPath(path);
        Assert.assertTrue(count > 0);
    }

    @Test
    public void shouldFindByTag() {
        Tags tag = tagsRepository.findFirstByTag(".webm");
        Assert.assertFalse(tag == null);
    }

    @Test
    public void shouldCountByTags() {
        Set<Tags> tags = List.of(".webm", ".mus2", "test2").stream().map(tag -> tagsRepository.findFirstByTag(tag)).filter(Objects::nonNull).collect(Collectors.toSet());
        long count = contentRepository.countByContentTagsInAndStorageIn(tags, Collections.singletonList(StorageType.LOCAL_STORAGE.name()));
        Assert.assertEquals(2, count);
    }

    @Test
    public void shouldFindByTags() {
        Set<Tags> tags = List.of(".webm", ".mus2", "test2").stream().map(tag -> tagsRepository.findFirstByTag(tag)).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Content> contents = contentRepository.findByContentTagsInAndStorageIn(tags, of(0, 2), Collections.singletonList(StorageType.LOCAL_STORAGE.name()));
        Assert.assertEquals(2, contents.size());
    }

    @Test
    public void shouldSaveContentId() {
        Content content = new Content();
        content.setPath("n");
        contentRepository.save(content);
        Video video = new Video();
        video.setName("n");
        video.setContentId(content.getId());
        videoRepository.save(video);
        content.setVideo(video);
        video = videoRepository.findFirstByName("n");
        Assert.assertEquals(content.getId(), video.getContentId());
    }
}
