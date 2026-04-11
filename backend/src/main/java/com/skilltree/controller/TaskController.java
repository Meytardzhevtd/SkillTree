package com.skilltree.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import com.skilltree.Service.TaskService;
import com.skilltree.Service.TaskSubmissionService;
import com.skilltree.dto.tasks.CreateTaskDto;
import com.skilltree.dto.tasks.SubmitAnswerRequest;
import com.skilltree.dto.tasks.SubmitAnswerResponse;
import com.skilltree.dto.tasks.TaskResponse;
import com.skilltree.dto.tasks.UpdateTaskDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/tasks")
@Validated
public class TaskController {

	private static final Logger log = LoggerFactory.getLogger(TaskController.class);

	private final TaskService taskService;
	private final TaskSubmissionService taskSubmissionService;

	public TaskController(TaskService taskService, TaskSubmissionService taskSubmissionService) {
		this.taskService = taskService;
		this.taskSubmissionService = taskSubmissionService;
	}

	@PostMapping
	public ResponseEntity<TaskResponse> create(@RequestBody @Valid CreateTaskDto createTaskDto,
			UriComponentsBuilder uriBuilder) {
		log.debug("Create task request: {}", createTaskDto);
		TaskResponse response = taskService.create(createTaskDto);
		URI location = uriBuilder.path("/api/tasks/{id}").buildAndExpand(response.getId()).toUri();
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	public ResponseEntity<TaskResponse> get(@PathVariable Long id) {
		TaskResponse resp = taskService.get(id);
		return ResponseEntity.ok(resp);
	}

	@PutMapping("/{id}")
	public ResponseEntity<TaskResponse> update(@PathVariable Long id,
			@RequestBody @Valid UpdateTaskDto updateTaskDto) {
		updateTaskDto.setId(id);
		TaskResponse resp = taskService.update(updateTaskDto);
		return ResponseEntity.ok(resp);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		taskService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<List<TaskResponse>> listByModule(
			@RequestParam(name = "moduleId") Long moduleId,
			@RequestParam(name = "progressModuleId", required = false) Long progressModuleId) {
		List<TaskResponse> list = taskService.getAllTasksByModule(moduleId, progressModuleId);
		return ResponseEntity.ok(list);
	}

	@PostMapping("/{id}/submit")
	public ResponseEntity<SubmitAnswerResponse> submit(@PathVariable Long id,
			@RequestBody @Valid SubmitAnswerRequest request) {
		log.info("Submit answer: taskId={}, progressModuleId={}", id, request.progressModuleId());
		SubmitAnswerResponse response = taskSubmissionService.submit(id, request);
		return ResponseEntity.ok(response);
	}
}