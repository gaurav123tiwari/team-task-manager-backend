package com.ttm.taskmanager.service;

import com.ttm.taskmanager.dto.DashboardDto;
import com.ttm.taskmanager.entity.Task.TaskStatus;
import com.ttm.taskmanager.entity.User;
import com.ttm.taskmanager.repository.ProjectRepository;
import com.ttm.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        User currentUser = authService.getCurrentUser();
        LocalDate today = LocalDate.now();

        if (currentUser.getRole() == User.Role.ADMIN) {
            long total = taskRepository.count();
            long completed = taskRepository.countByAssignedToIdAndStatus(null, TaskStatus.DONE);
            // For admin: count all tasks
            long allCompleted = countAllByStatus(TaskStatus.DONE);
            long allInProgress = countAllByStatus(TaskStatus.IN_PROGRESS);
            long allTodo = countAllByStatus(TaskStatus.TODO);
            long overdue = taskRepository.countAllOverdue(today);
            long totalProjects = projectRepository.count();

            return DashboardDto.builder()
                .totalTasks(total)
                .completedTasks(allCompleted)
                .pendingTasks(allTodo)
                .inProgressTasks(allInProgress)
                .overdueTasks(overdue)
                .totalProjects(totalProjects)
                .build();
        } else {
            Long userId = currentUser.getId();
            long total = taskRepository.countByAssignedToId(userId);
            long completed = taskRepository.countByAssignedToIdAndStatus(userId, TaskStatus.DONE);
            long inProgress = taskRepository.countByAssignedToIdAndStatus(userId, TaskStatus.IN_PROGRESS);
            long pending = taskRepository.countByAssignedToIdAndStatus(userId, TaskStatus.TODO);
            long overdue = taskRepository.countOverdueByUser(userId, today);
            long totalProjects = projectRepository.findAllProjectsForUser(userId).size();

            return DashboardDto.builder()
                .totalTasks(total)
                .completedTasks(completed)
                .pendingTasks(pending)
                .inProgressTasks(inProgress)
                .overdueTasks(overdue)
                .totalProjects(totalProjects)
                .build();
        }
    }

    private long countAllByStatus(TaskStatus status) {
        return taskRepository.findAll().stream()
            .filter(t -> t.getStatus() == status)
            .count();
    }
}
