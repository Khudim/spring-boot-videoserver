package com.khudim.dao.repository;

import com.khudim.dao.entity.Content;
import com.khudim.dao.entity.Tags;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by Beaver.
 */
@Repository
public interface ContentRepository extends JpaRepository<Content, Long>, CrudRepository<Content, Long> {

    @Query("SELECT c.image FROM Content c WHERE c.id = :contentId")
    byte[] findImageById(@Param("contentId") long contentId);

    long countByPath(String path);

    long countByStorageIn(List<String> fileStorages);

    long countByContentTagsInAndStorageIn(Set<Tags> contentTags, List<String> fileStorages);

    List<Content> findByStorageIn(Pageable pageable, List<String> fileStorages);

    List<Content> findByContentTagsInAndStorageIn(Set<Tags> tags, Pageable pageable, List<String> fileStorages);
}
