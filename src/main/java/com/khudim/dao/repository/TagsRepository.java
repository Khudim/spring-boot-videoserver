package com.khudim.dao.repository;

import com.khudim.dao.entity.Tags;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagsRepository extends CrudRepository<Tags, Long> {

    Tags findFirstByTag(String tag);
}
