package com.khudim.dao;

import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

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
        video1.setTag("tEst");
        Video video2 = new Video();
        video2.setTag("fail");
        Video video3 = new Video();
        video3.setTag("test2");

        entityManager.persist(video1);
        entityManager.persist(video2);
        entityManager.persist(video3);

        List<Video> videos = videoRepository.findByTagIgnoreCase("test", new PageRequest(0, 2));
        System.out.println(videos);
    }
}
