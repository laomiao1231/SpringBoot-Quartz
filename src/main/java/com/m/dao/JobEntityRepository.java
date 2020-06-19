package com.m.dao;

import com.m.entity.JobEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobEntityRepository extends CrudRepository<JobEntity, Long> {
    JobEntity getById(Integer id);
}
