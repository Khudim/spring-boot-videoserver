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
}
