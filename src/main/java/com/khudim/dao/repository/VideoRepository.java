package com.khudim.dao.repository;

import com.khudim.dao.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Beaver.
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long>, CrudRepository<Video, Long> {

    Video findFirstByName(String name);

//    @Query("SELECT v FROM Video v WHERE v.tags IN (:tags)")
    //   List<Video> findByTags(@Param("tags") List<Tags> tags, Pageable pageable);

    // List<Video> findByTagsIgnoreCase(String tag, Pageable pageable);

    // @Query("SELECT COUNT(v) FROM Video v WHERE v.tags IN (:tags)")
    // Long countByTags(@Param("tags") List<Tags> tags);
}
