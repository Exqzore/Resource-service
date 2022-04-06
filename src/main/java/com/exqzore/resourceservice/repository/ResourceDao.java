package com.exqzore.resourceservice.repository;

import com.exqzore.resourceservice.model.domain.ResourceInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceDao extends JpaRepository<ResourceInfo, Long> {

    List<ResourceInfo> deleteAllByIdIn(List<Long> ids);
}
