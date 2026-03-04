package com.skilltree.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.dto.tasks.CreateTaskDto;
import com.skilltree.dto.tasks.TaskResponse;
import com.skilltree.dto.tasks.UpdateTaskDto;
import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.model.Module;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.TaskTypeRepository;

@Transactional(readOnly = true)
@Service
public class TaskService {
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

	private Task getTaskOrThrow(Long taskId) {
		Optional<Task> oTask = taskRepository.findById(taskId);
		if (oTask.isPresent()) {
			return oTask.get();
		} else {
			throw new TaskNotFoundException(taskId);
		}
	}

	@Transactional
	public TaskResponse create(CreateTaskDto createTaskDto) {
		Optional<TaskTypes> taskType = taskTypeRepository.findById(createTaskDto.getTaskTypeId());
		Optional<Module> module = moduleRepository.findById(createTaskDto.getModuleId());
		if (!taskType.isPresent() || !module.isPresent()) {
			throw new RuntimeException("TODO: потом сделаю кастомные исключения, пока лень");
		}
		Task saved = taskRepository.save(createTaskDto.toEntity(taskType.get(), module.get()));
		if (saved == null) {
			throw new RuntimeException("Error");
		}
		return new TaskResponse(saved.getId(), saved.getTask_type().getId(), saved.getModule().getId(),
				saved.getContent());
	}

	public TaskResponse get(Long taskId) {
		Task task = getTaskOrThrow(taskId);
		return TaskResponse.of(task);
	}

	@Transactional
	public TaskResponse update(UpdateTaskDto updateTaskDto) {
		Task task = getTaskOrThrow(updateTaskDto.getId());
		Optional<TaskTypes> oTaskType = taskTypeRepository.findById(updateTaskDto.getTaskTypeId());
		if (oTaskType.isPresent() == false) {
			throw new RuntimeException("TODO: надо кастомные");
		}
		task.setTask_type(oTaskType.get());
		task.setContent(updateTaskDto.getContent());
		return TaskResponse.of(taskRepository.save(task));
	}

	@Transactional
	public void delete(Long id) {
		Task task = getTaskOrThrow(id);
		taskRepository.delete(task);
	}

	public List<TaskResponse> getAllTasksByModule(Long moduleId) {
		Optional<Module> oModule = moduleRepository.findById(moduleId);
		if (oModule.isPresent() == false) {
			throw new RuntimeException("TODO");
		}
		return taskRepository.findByModule(oModule.get()).stream()
				.map((task) -> TaskResponse.of(task))
				.sorted((p1, p2) -> Long.compare(p1.getId(), p2.getId()))
				.collect(Collectors.toList());

	}
}
