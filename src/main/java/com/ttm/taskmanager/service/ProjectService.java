package com.ttm.taskmanager.service;

import com.ttm.taskmanager.dto.AuthDto;
import com.ttm.taskmanager.dto.ProjectDto;
import com.ttm.taskmanager.entity.Project;
import com.ttm.taskmanager.entity.User;
import com.ttm.taskmanager.exception.AccessDeniedException;
import com.ttm.taskmanager.exception.ResourceNotFoundException;
import com.ttm.taskmanager.repository.ProjectRepository;
import com.ttm.taskmanager.repository.TaskRepository;
import com.ttm.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;

    @Transactional
    public ProjectDto.Response createProject(ProjectDto.CreateRequest request) {

        User currentUser = authService.getCurrentUser();

        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only admins can create projects");
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .members(new HashSet<>()) // safe init
                .build();

        // extra safety (double protection)
        if (project.getMembers() == null) {
            project.setMembers(new HashSet<>());
        }

        project.getMembers().add(currentUser);

        return toResponse(projectRepository.save(project));
    }

    @Transactional(readOnly = true)
    public List<ProjectDto.Response> getProjects() {
        User currentUser = authService.getCurrentUser();
        List<Project> projects;

        if (currentUser.getRole() == User.Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findAllProjectsForUser(currentUser.getId());
        }

        return projects.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDto.Response getProjectById(Long projectId) {
        User currentUser = authService.getCurrentUser();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        if (currentUser.getRole() != User.Role.ADMIN &&
                project.getMembers().stream().noneMatch(m -> m.getId().equals(currentUser.getId()))) {
            throw new AccessDeniedException("You don't have access to this project");
        }

        return toResponse(project);
    }

    @Transactional
    public ProjectDto.Response addMember(Long projectId, ProjectDto.AddMemberRequest request) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only admins can add members");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User member = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getUserId()));

        project.getMembers().add(member);
        projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public ProjectDto.Response removeMember(Long projectId, Long userId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only admins can remove members");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        project.getMembers().remove(member);
        projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    public void deleteProject(Long projectId) {
        User currentUser = authService.getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only admins can delete projects");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));

        projectRepository.delete(project);
    }

    private ProjectDto.Response toResponse(Project project) {
        ProjectDto.Response response = new ProjectDto.Response();
        response.setId(project.getId());
        response.setName(project.getName());
        response.setDescription(project.getDescription());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        response.setTaskCount((int) taskRepository.countByProjectId(project.getId()));

        // createdBy
        AuthDto.UserDto createdByDto = new AuthDto.UserDto();
        createdByDto.setId(project.getCreatedBy().getId());
        createdByDto.setName(project.getCreatedBy().getName());
        createdByDto.setEmail(project.getCreatedBy().getEmail());
        createdByDto.setRole(project.getCreatedBy().getRole());
        response.setCreatedBy(createdByDto);

        // members
        Set<AuthDto.UserDto> memberDtos = project.getMembers().stream().map(m -> {
            AuthDto.UserDto dto = new AuthDto.UserDto();
            dto.setId(m.getId());
            dto.setName(m.getName());
            dto.setEmail(m.getEmail());
            dto.setRole(m.getRole());
            return dto;
        }).collect(Collectors.toSet());
        response.setMembers(memberDtos);

        return response;
    }
}
