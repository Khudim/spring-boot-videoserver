package com.khudim.dao;

import com.khudim.dao.entity.Content;
import com.khudim.dao.repository.ContentRepository;
import com.khudim.dao.repository.VideoRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

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

    private String path = "path";

    @Before
    public void setup(){
        Content expectedContent = new Content();
        expectedContent.setPath(path);
        this.entityManager.persist(expectedContent);
    }

    @Test
    public void shouldFindByPath() {
        Long count = contentRepository.countByPath(path);
        Assert.assertTrue(count > 0);
    }
}
