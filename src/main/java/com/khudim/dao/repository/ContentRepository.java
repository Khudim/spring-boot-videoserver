package com.khudim.dao.repository;

import com.khudim.dao.entity.Content;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Beaver.
 */
@Repository
public interface ContentRepository extends CrudRepository<Content, Long> {

    @Query("SELECT c.image FROM Content c WHERE c.id = :contentId")
    byte[] findImageById(@Param("contentId") long contentId);

    @Query("SELECT c.path FROM Content c WHERE c.id = :contentId")
    String findPathById(@Param("contentId") long contentId);

    Long countByPath(String path);

   //@Query("SELECT COUNT(c) FROM Content c WHERE c.tag IN (:tags)")
 //   Long countByTag(@Param("tags") List<String> tags);
}
