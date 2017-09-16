package com.khudim.dao;

import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

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

    @Before
    public void setup() {
        Video video1 = new Video();
        video1.setTag("tEst");
        video1.setName("name");
        Video video2 = new Video();
        video2.setTag("fail");
        video2.setName("name");
        Video video3 = new Video();
        video3.setTag("test2");

        entityManager.persist(video1);
        entityManager.persist(video2);
        entityManager.persist(video3);
    }

    @Test
    public void shouldFindByTag() {
        List<Video> videos = videoRepository.findByTagIgnoreCase("test2", new PageRequest(0, 2));
        Assert.assertFalse(videos.isEmpty());
    }

    @Test
    public void shouldFindByTags() {
        List<Video> videos = videoRepository.findByTags(Arrays.asList("test", "fail", "test2"), new PageRequest(0, 2));
        Assert.assertEquals(2, videos.size());
    }

    @Test
    public void shouldFindOne() {
        Video video = videoRepository.findFirstByName("name");
        Assert.assertNotNull(video);
    }

    @Test
    public void shouldCountByTags() {
        long count = videoRepository.countByTags(Arrays.asList("fail", "test2"));
        Assert.assertEquals(2, count);
    }
}
