package com.khudim.dao;

import com.khudim.dao.entity.Content;
import com.khudim.dao.repository.ContentRepository;
import org.junit.Assert;
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

    @Test
    public void findByPathTest() {
        String path = "/path/to/file";
        Content expectedContent = new Content();
        expectedContent.setPath(path);
        this.entityManager.persist(expectedContent);
        Content testContent = contentRepository.findByPath(path);
        Assert.assertNotNull(testContent);
    }

    @Test
    public void findPathByIdTest() {
        String path = "/path/to/file";
        Content expectedContent = new Content();
        expectedContent.setPath(path);
        this.entityManager.persist(expectedContent);
        String testContent = contentRepository.findPathById(1);
        Assert.assertNotNull(testContent);
    }
}
