package com.skilltree.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import com.skilltree.Service.TaskService;
import com.skilltree.dto.tasks.CreateTaskDto;
import com.skilltree.dto.tasks.TaskResponse;
import com.skilltree.dto.tasks.UpdateTaskDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/tasks")
@Validated
public class TaskController {
	private static final Logger log = LoggerFactory.getLogger(TaskController.class);
	private final TaskService taskService;

	public TaskController(TaskService taskService) {
		this.taskService = taskService;
	}

	@PostMapping
	public ResponseEntity<TaskResponse> create(@RequestBody @Valid CreateTaskDto createTaskDto,
			UriComponentsBuilder uriBuilder) {
		log.debug("Create task request: {}", createTaskDto);
		log.info("!!!!!!! RECEIVED TASK: {}", createTaskDto); // ← ДОБАВИТЬ
		log.info("!!!!!!! CONTENT: {}", createTaskDto.getContent()); // ← ДОБАВИТЬ
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
			@RequestParam(name = "moduleId") Long moduleId) {
		List<TaskResponse> list = taskService.getAllTasksByModule(moduleId);
		return ResponseEntity.ok(list);
	}
}
