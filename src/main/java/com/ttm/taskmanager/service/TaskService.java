package com.ttm.taskmanager.service;

import com.ttm.taskmanager.dto.AuthDto;
import com.ttm.taskmanager.dto.TaskDto;
import com.ttm.taskmanager.entity.Project;
import com.ttm.taskmanager.entity.Task;
import com.ttm.taskmanager.entity.Task.TaskStatus;
import com.ttm.taskmanager.entity.User;
import com.ttm.taskmanager.exception.AccessDeniedException;
import com.ttm.taskmanager.exception.ResourceNotFoundException;
import com.ttm.taskmanager.repository.ProjectRepository;
import com.ttm.taskmanager.repository.TaskRepository;
import com.ttm.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Transactional
    public TaskDto.Response createTask(TaskDto.CreateRequest request) {
        User currentUser = authService.getCurrentUser();

        Project project = projectRepository.findById(request.getProjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only admins can create tasks");
        }

        User assignedTo = null;
        if (request.getAssignedToId() != null) {
            assignedTo = userRepository.findById(request.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedToId()));
        }

        Task task = Task.builder()
            .title(request.getTitle())
            .description(request.getDescription())
            .status(request.getStatus())
            .dueDate(request.getDueDate())
            .project(project)
            .assignedTo(assignedTo)
            .createdBy(currentUser)
            .build();

        taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional
    public TaskDto.Response updateTask(Long taskId, TaskDto.UpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (currentUser.getRole() == User.Role.MEMBER) {
            if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You can only update tasks assigned to you");
            }
            if (request.getStatus() != null) task.setStatus(request.getStatus());
        } else {
            if (request.getTitle() != null) task.setTitle(request.getTitle());
            if (request.getDescription() != null) task.setDescription(request.getDescription());
            if (request.getStatus() != null) task.setStatus(request.getStatus());
            if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
            if (request.getAssignedToId() != null) {
                User newAssignee = userRepository.findById(request.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssignedToId()));
                task.setAssignedTo(newAssignee);
            }
        }

        taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional
    public TaskDto.Response updateTaskStatus(Long taskId, TaskDto.StatusUpdateRequest request) {
        User currentUser = authService.getCurrentUser();
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (currentUser.getRole() == User.Role.MEMBER &&
            (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You can only update status of tasks assigned to you");
        }

        task.setStatus(request.getStatus());
        taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskDto.PagedResponse getTasksByProject(Long projectId, int page, int size,
                                                    String status, String search) {
        User currentUser = authService.getCurrentUser();
        Project project = projectRepository.findById(projectId)
            .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (currentUser.getRole() != User.Role.ADMIN &&
            project.getMembers().stream().noneMatch(m -> m.getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        TaskStatus taskStatus = status != null && !status.isBlank() ? TaskStatus.valueOf(status) : null;
        String searchTerm = search != null && !search.isBlank() ? search : null;

        Page<Task> taskPage = taskRepository.searchTasks(projectId, taskStatus, searchTerm, pageable);
        return toPagedResponse(taskPage);
    }

    @Transactional(readOnly = true)
    public TaskDto.PagedResponse getMyTasks(int page, int size, String status, String search) {
        User currentUser = authService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        TaskStatus taskStatus = status != null && !status.isBlank() ? TaskStatus.valueOf(status) : null;
        String searchTerm = search != null && !search.isBlank() ? search : null;

        Page<Task> taskPage = taskRepository.searchTasksByUser(
            currentUser.getId(), taskStatus, searchTerm, pageable);
        return toPagedResponse(taskPage);
    }

    @Transactional(readOnly = true)
    public TaskDto.Response getTaskById(Long taskId) {
        User currentUser = authService.getCurrentUser();
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (currentUser.getRole() == User.Role.MEMBER &&
            (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("Access denied to this task");
        }

        return toResponse(task);
    }

    @Transactional
    public void deleteTask(Long taskId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only admins can delete tasks");
        }
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        taskRepository.delete(task);
    }

    private TaskDto.Response toResponse(Task task) {
        TaskDto.Response response = new TaskDto.Response();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setDueDate(task.getDueDate());
        response.setProjectId(task.getProject().getId());
        response.setProjectName(task.getProject().getName());
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());

        LocalDate today = LocalDate.now();
        response.setOverdue(task.getDueDate() != null
            && task.getDueDate().isBefore(today)
            && task.getStatus() != TaskStatus.DONE);

        if (task.getAssignedTo() != null) {
            AuthDto.UserDto dto = new AuthDto.UserDto();
            dto.setId(task.getAssignedTo().getId());
            dto.setName(task.getAssignedTo().getName());
            dto.setEmail(task.getAssignedTo().getEmail());
            dto.setRole(task.getAssignedTo().getRole());
            response.setAssignedTo(dto);
        }

        if (task.getCreatedBy() != null) {
            AuthDto.UserDto dto = new AuthDto.UserDto();
            dto.setId(task.getCreatedBy().getId());
            dto.setName(task.getCreatedBy().getName());
            dto.setEmail(task.getCreatedBy().getEmail());
            dto.setRole(task.getCreatedBy().getRole());
            response.setCreatedBy(dto);
        }

        return response;
    }

    private TaskDto.PagedResponse toPagedResponse(Page<Task> page) {
        TaskDto.PagedResponse response = new TaskDto.PagedResponse();
        response.setContent(page.getContent().stream().map(this::toResponse).collect(Collectors.toList()));
        response.setPageNumber(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());
        return response;
    }
}
