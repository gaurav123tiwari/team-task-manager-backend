package com.ttm.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

public class ProjectDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Project name is required")
        @Size(max = 150)
        private String name;

        private String description;
    }

    @Data
    public static class Response {
        private Long id;
        private String name;
        private String description;
        private AuthDto.UserDto createdBy;
        private Set<AuthDto.UserDto> members;
        private int taskCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    public static class AddMemberRequest {
        private Long userId;
    }
}
