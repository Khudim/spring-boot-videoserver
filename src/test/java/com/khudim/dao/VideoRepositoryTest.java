package com.khudim.dao;

import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import com.khudim.dao.service.VideoService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import java.awt.print.Pageable;
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

    @Test
    public void shouldFindByTags() {
        Video video1 = new Video();
        video1.setTags("test");
        Video video2 = new Video();
        video2.setTags("fail");
        Video video3 = new Video();
        video3.setTags("test2");

        entityManager.persist(video1);
        entityManager.persist(video2);
        entityManager.persist(video3);
        List<Video> videos = videoRepository.findAll(new PageRequest(1, 10)).getContent();
        Assert.assertEquals(2, videos.size());
    }
}
