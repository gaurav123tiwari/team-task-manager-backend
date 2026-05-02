package com.ttm.taskmanager.repository;

import com.ttm.taskmanager.entity.Project;
import com.ttm.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCreatedBy(User createdBy);

    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.id = :userId")
    List<Project> findProjectsByMemberId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.id = :userId OR p.createdBy.id = :userId")
    List<Project> findAllProjectsForUser(@Param("userId") Long userId);
}
