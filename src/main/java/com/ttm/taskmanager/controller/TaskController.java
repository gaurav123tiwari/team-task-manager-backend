package com.ttm.taskmanager.controller;

import com.ttm.taskmanager.dto.TaskDto;
import com.ttm.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskDto.Response> createTask(@Valid @RequestBody TaskDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<TaskDto.PagedResponse> getTasksByProject(
            @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId, page, size, status, search));
    }

    @GetMapping("/my")
    public ResponseEntity<TaskDto.PagedResponse> getMyTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(taskService.getMyTasks(page, size, status, search));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TaskDto.Response> getTaskById(@PathVariable Long taskId) {
        return ResponseEntity.ok(taskService.getTaskById(taskId));
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskDto.Response> updateTask(
            @PathVariable Long taskId,
            @RequestBody TaskDto.UpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTask(taskId, request));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<TaskDto.Response> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskDto.StatusUpdateRequest request) {
        return ResponseEntity.ok(taskService.updateTaskStatus(taskId, request));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
