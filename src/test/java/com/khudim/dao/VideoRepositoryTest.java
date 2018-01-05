package com.khudim.dao;


import com.khudim.dao.entity.Tags;
import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.TagsRepository;
import com.khudim.dao.repository.VideoRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author hudyshkin.
 */
@RunWith(SpringRunner.class)
@Ignore
@DataJpaTest
public class VideoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private TagsRepository tagsRepository;


    @Before
    public void setup() {
        Tags tags1 = new Tags(".webm");
        Tags tags2 = new Tags(".fap");
        Set<Tags> set = new HashSet<>();
        set.add(tags1);
        set.add(tags2);
        Video video1 = new Video();
        video1.setVideoTags(set);
        video1.setName("name");
        Video video2 = new Video();
        video2.setVideoTags(set);
        video2.setName("name");
        Video video3 = new Video();
        video3.setVideoTags(Set.of(tags2));
        Set<Video> videos = new HashSet<>();
        videos.add(video1);
        tags1.setVideos(videos);
        entityManager.persist(video1);
        entityManager.persist(video2);
        entityManager.persist(video3);
    }

    @Test
    public void shouldFindByTag() {
        Tags tag = tagsRepository.findFirstByTag(".webm");
        Assert.assertFalse(tag == null);
    }

    @Test
    public void shouldFindByTags() {
        Set<Tags> tags = List.of(".webm", "fap", "test2").stream().map(tag -> tagsRepository.findFirstByTag(tag)).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Video> videos = videoRepository.findByVideoTags(tags, new PageRequest(0, 2));
        Assert.assertEquals(2, videos.size());
    }

    @Test
    public void shouldFindOne() {
        Video video = videoRepository.findFirstByName("name");
        Assert.assertNotNull(video);
    }

    @Test
    public void shouldCountByTags() {
        Set<Tags> tags = List.of(".webm", "fap", "test2").stream().map(tag -> tagsRepository.findFirstByTag(tag)).filter(Objects::nonNull).collect(Collectors.toSet());
        long count = videoRepository.countByVideoTags(tags);
        Assert.assertEquals(2, count);
    }
}
