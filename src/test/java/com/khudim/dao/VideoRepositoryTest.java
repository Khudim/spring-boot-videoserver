package com.khudim.dao;


import com.khudim.dao.entity.Tags;
import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.TagsRepository;
import com.khudim.dao.repository.VideoRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashSet;
import java.util.Set;

/**
 * @author hudyshkin.
 */
@RunWith(SpringRunner.class)
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
        System.out.println(tagsRepository.findAll());
        System.out.println(tag.getVideos());
        //List<Video> videos = videoRepository.findByTagIgnoreCase("test2", new PageRequest(0, 2));
    }

    @Test
    public void shouldFindByTags() {
        //       List<Video> videos = videoRepository.findByTags(Arrays.asList("test", "fail", "test2"), new PageRequest(0, 2));
        //      Assert.assertEquals(2, videos.size());
    }

    @Test
    public void shouldFindOne() {
        Video video = videoRepository.findFirstByName("name");
        Assert.assertNotNull(video);
    }

    @Test
    public void shouldCountByTags() {
        //    long count = videoRepository.countByTags(Arrays.asList("fail", "test2"));
        //   Assert.assertEquals(2, count);
    }
}
