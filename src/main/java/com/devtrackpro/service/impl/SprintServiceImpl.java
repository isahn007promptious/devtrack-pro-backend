package com.devtrackpro.service.impl;

import com.devtrackpro.dto.SprintRequest;
import com.devtrackpro.dto.SprintResponse;
import com.devtrackpro.entity.Project;
import com.devtrackpro.entity.Sprint;
import com.devtrackpro.entity.SprintStatus;
import com.devtrackpro.exception.ResourceNotFoundException;
import com.devtrackpro.repository.ProjectRepository;
import com.devtrackpro.repository.SprintRepository;
import com.devtrackpro.service.SprintService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;

    public SprintServiceImpl(SprintRepository sprintRepository,
                             ProjectRepository projectRepository) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public SprintResponse createSprint(Long projectId, SprintRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        Sprint sprint = Sprint.builder()
                .project(project)
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.valueOf(request.getStatus()))
                .build();

        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse getSprint(Long id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));
        return mapToResponse(sprint);
    }

    @Override
    public SprintResponse updateSprint(Long id, SprintRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        sprint.setName(request.getName());
        sprint.setGoal(request.getGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setStatus(SprintStatus.valueOf(request.getStatus()));

        Sprint saved = sprintRepository.save(sprint);
        return mapToResponse(saved);
    }

    @Override
    public void deleteSprint(Long id) {
        if (!sprintRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sprint not found");
        }
        sprintRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getProjectSprints(Long projectId) {
        if (!projectRepository.existsById(projectId)) {
            throw new ResourceNotFoundException("Project not found");
        }
        return sprintRepository.findByProjectId(projectId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SprintResponse mapToResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .projectId(sprint.getProject().getId())
                .name(sprint.getName())
                .goal(sprint.getGoal())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .status(sprint.getStatus().name())
                .createdAt(sprint.getCreatedAt())
                .build();
    }
}
