package com.ttm.taskmanager.dto;

import com.ttm.taskmanager.entity.Task.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TaskDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Task title is required")
        @Size(max = 200)
        private String title;

        private String description;

        @NotNull(message = "Status is required")
        private TaskStatus status = TaskStatus.TODO;

        private LocalDate dueDate;

        @NotNull(message = "Project ID is required")
        private Long projectId;

        private Long assignedToId;
    }

    @Data
    public static class UpdateRequest {
        @Size(max = 200)
        private String title;

        private String description;

        private TaskStatus status;

        private LocalDate dueDate;

        private Long assignedToId;
    }

    @Data
    public static class StatusUpdateRequest {
        @NotNull(message = "Status is required")
        private TaskStatus status;
    }

    @Data
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private TaskStatus status;
        private LocalDate dueDate;
        private Long projectId;
        private String projectName;
        private AuthDto.UserDto assignedTo;
        private AuthDto.UserDto createdBy;
        private boolean overdue;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class PagedResponse {
        private java.util.List<Response> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean last;
    }
}
