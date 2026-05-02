package com.ttm.taskmanager.repository;

import com.ttm.taskmanager.entity.Task;
import com.ttm.taskmanager.entity.Task.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByProjectId(Long projectId, Pageable pageable);

    List<Task> findByAssignedToId(Long userId);

    Page<Task> findByAssignedToId(Long userId, Pageable pageable);

    List<Task> findByProjectIdAndAssignedToId(Long projectId, Long userId);

    long countByProjectId(Long projectId);

    long countByProjectIdAndStatus(Long projectId, TaskStatus status);

    long countByAssignedToId(Long userId);

    long countByAssignedToIdAndStatus(Long userId, TaskStatus status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.assignedTo.id = :userId AND t.dueDate < :today AND t.status != 'DONE'")
    long countOverdueByUser(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.id = :projectId AND t.dueDate < :today AND t.status != 'DONE'")
    long countOverdueByProject(@Param("projectId") Long projectId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :today AND t.status != 'DONE'")
    long countAllOverdue(@Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.project.id = :projectId AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Task> searchTasks(@Param("projectId") Long projectId,
                           @Param("status") TaskStatus status,
                           @Param("search") String search,
                           Pageable pageable);

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId AND " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Task> searchTasksByUser(@Param("userId") Long userId,
                                 @Param("status") TaskStatus status,
                                 @Param("search") String search,
                                 Pageable pageable);
}
