package com.khudim.dao.repository;

import com.khudim.dao.entity.Content;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Created by Beaver.
 */
@Repository
public interface ContentRepository extends CrudRepository<Content, Long> {

    @Query("SELECT c.image FROM Content c WHERE c.id = :contentId")
    byte[] getImage(@Param("contentId") long contentId);

    @Query("SELECT c.path FROM Content c WHERE c.id = :contentId")
    String getVideoPath(@Param("contentId") long contentId);

    @Query("SELECT c FROM Content c WHERE c.path = :path")
    Content getContentByPath(@Param("path") String path);
}
