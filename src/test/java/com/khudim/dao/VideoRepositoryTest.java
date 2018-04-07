package com.khudim.dao;


import com.khudim.dao.entity.Video;
import com.khudim.dao.repository.VideoRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author hudyshkin.
 */
@RunWith(SpringRunner.class)
@Ignore
@DataJpaTest
public class VideoRepositoryTest {

    @Autowired
    private VideoRepository videoRepository;


    @Before
    public void setup() {

    }


    @Test
    public void shouldFindOne() {
        Video video = videoRepository.findFirstByName("name");
        Assert.assertNotNull(video);
    }

}
