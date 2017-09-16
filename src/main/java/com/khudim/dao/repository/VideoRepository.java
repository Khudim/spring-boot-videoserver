package com.khudim.dao.repository;

import com.khudim.dao.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Beaver.
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long>, CrudRepository<Video, Long> {

    Long countByName(String name);

    @Query("SELECT v FROM Video v WHERE v.tag IN (:tags)")
    List<Video> findByTags(@Param("tags") List<String> tags, Pageable pageable);

    List<Video> findByTagIgnoreCase(String tag, Pageable pageable);
}
