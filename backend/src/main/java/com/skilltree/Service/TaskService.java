package com.skilltree.Service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.dto.tasks.CreateTaskDto;
import com.skilltree.dto.tasks.TaskResponse;
import com.skilltree.dto.tasks.UpdateTaskDto;
import com.skilltree.exception.ModuleNotFoundException;
import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.exception.TaskTypesNotFound;
import com.skilltree.model.Module;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.TaskTypeRepository;

@Transactional(readOnly = true)
@Service
public class TaskService {
	private static final Logger log = LoggerFactory.getLogger(TaskService.class);

	private final TaskRepository taskRepository;
	private final TaskTypeRepository taskTypeRepository;
	private final ModuleRepository moduleRepository;

	@Autowired
	public TaskService(TaskRepository taskRepository, TaskTypeRepository taskTypeRepository,
			ModuleRepository moduleRepository) {
		this.taskRepository = taskRepository;
		this.taskTypeRepository = taskTypeRepository;
		this.moduleRepository = moduleRepository;
	}

	@Transactional
	public TaskResponse create(CreateTaskDto createTaskDto) {
		log.info("Создание задания, taskTypeId={}, moduleId={}", createTaskDto.getTaskTypeId(),
				createTaskDto.getModuleId());

		TaskTypes taskType = taskTypeRepository.findById(createTaskDto.getTaskTypeId())
				.orElseThrow(() -> new TaskTypesNotFound(createTaskDto.getTaskTypeId()));

		Module module = moduleRepository.findById(createTaskDto.getModuleId())
				.orElseThrow(() -> new ModuleNotFoundException(createTaskDto.getModuleId()));

		Task saved = taskRepository.save(createTaskDto.toEntity(taskType, module));

		log.info("Задание создано, id={}", saved.getId());
		return TaskResponse.of(saved);
	}

	public TaskResponse get(Long taskId) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new TaskNotFoundException(taskId));
		return TaskResponse.of(task);
	}

	@Transactional
	public TaskResponse update(UpdateTaskDto updateTaskDto) {
		Task task = taskRepository.findById(updateTaskDto.getId())
				.orElseThrow(() -> new TaskNotFoundException(updateTaskDto.getId()));

		TaskTypes taskType = taskTypeRepository.findById(updateTaskDto.getTaskTypeId())
				.orElseThrow(() -> new TaskTypesNotFound(updateTaskDto.getTaskTypeId()));

		task.setTask_type(taskType);
		task.setContent(updateTaskDto.getContent()); // напрямую, без getContentAsMap()

		Task saved = taskRepository.save(task);
		return TaskResponse.of(saved);
	}

	@Transactional
	public void delete(Long id) {
		Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
		taskRepository.delete(task);
	}

	public List<TaskResponse> getAllTasksByModule(Long moduleId) {
		Module module = moduleRepository.findById(moduleId)
				.orElseThrow(() -> new ModuleNotFoundException(moduleId));

		return taskRepository.findByModule(module).stream().map(TaskResponse::of)
				.sorted((p1, p2) -> Long.compare(p1.getId(), p2.getId()))
				.collect(Collectors.toList());
	}
}