package com.ttm.taskmanager.controller;

import com.ttm.taskmanager.dto.ProjectDto;
import com.ttm.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto.Response> createProject(@Valid @RequestBody ProjectDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @GetMapping
    public ResponseEntity<List<ProjectDto.Response>> getProjects() {
        return ResponseEntity.ok(projectService.getProjects());
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDto.Response> getProjectById(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<ProjectDto.Response> addMember(
            @PathVariable Long projectId,
            @RequestBody ProjectDto.AddMemberRequest request) {
        return ResponseEntity.ok(projectService.addMember(projectId, request));
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    public ResponseEntity<ProjectDto.Response> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {
        return ResponseEntity.ok(projectService.removeMember(projectId, userId));
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}
