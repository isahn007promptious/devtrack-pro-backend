package com.devtrackpro.repository;

import com.devtrackpro.entity.Task;
import com.devtrackpro.entity.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    long countByProjectId(Long projectId);
    long countByProjectIdAndStatus(Long projectId, TaskStatus status);
    
    List<Task> findBySprintId(Long sprintId);
    List<Task> findByProjectId(Long projectId);
    Page<Task> findBySprintId(Long sprintId, Pageable pageable);

    List<Task> findByDueDateAndStatusNotAndAssigneeNotNull(java.time.LocalDate dueDate, TaskStatus status);

    long countByProjectIdIn(List<Long> projectIds);
    long countByProjectIdInAndStatus(List<Long> projectIds, TaskStatus status);
    long countByProjectIdInAndStatusNot(List<Long> projectIds, TaskStatus status);
    long countByProjectIdInAndStatusNotAndDueDateBefore(List<Long> projectIds, TaskStatus status, java.time.LocalDate date);
    List<Task> findByProjectIdInAndStatusAndUpdatedAtAfter(List<Long> projectIds, TaskStatus status, java.time.LocalDateTime date);

    @org.springframework.data.jpa.repository.Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds AND " +
           "(t.title LIKE %:q% OR t.description LIKE %:q% OR t.taskKey LIKE %:q%)")
    List<Task> searchTasksInProjects(@org.springframework.data.repository.query.Param("projectIds") List<Long> projectIds, @org.springframework.data.repository.query.Param("q") String q);

    // Queries to support overdue check
    // We will build custom queries or use Spring Data
}
