package com.khudim.dao.repository;

import com.khudim.dao.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Beaver.
 */
@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

    @Override
    Page<Video> findAll(Pageable pageable);

    Long countByName(String name);

    List<Video> findByTagIgnoreCase(String tag, Pageable pageable);
}
