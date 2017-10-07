package com.khudim.dao.repository;

import com.khudim.dao.entity.Tags;
import com.khudim.dao.entity.Video;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * Created by Beaver.
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long>, CrudRepository<Video, Long> {

    Video findFirstByName(String name);

    List<Video> findByVideoTags(Set<Tags> tags, Pageable pageable);

    Long countByVideoTags(Set<Tags> tags);
}
