package com.skilltree.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.controller.TaskController;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Transactional(readOnly = true)
@Service
public class TaskService {
	private final TaskRepository taskRepository;
	private final TaskTypeRepository taskTypeRepository;
	private final ModuleRepository moduleRepository;

	private static final Logger log = LoggerFactory.getLogger(TaskService.class);

	@Autowired
	public TaskService(TaskRepository taskRepository, TaskTypeRepository taskTypeRepository,
			ModuleRepository moduleRepository) {
		this.taskRepository = taskRepository;
		this.taskTypeRepository = taskTypeRepository;
		this.moduleRepository = moduleRepository;
	}

	@Transactional
	public TaskResponse create(CreateTaskDto createTaskDto) {
		log.info("начало create task");

		// Проверяем и добавляем TaskTypes если их нет
		TaskTypes taskType = ensureTaskTypeExists(createTaskDto.getTaskTypeId());

		log.debug("task type найден: {}", taskType.getName());

		Module module = moduleRepository.findById(createTaskDto.getModuleId())
				.orElseThrow(() -> new ModuleNotFoundException(createTaskDto.getModuleId()));

		log.debug("module найден");

		Task saved = taskRepository.save(createTaskDto.toEntity(taskType, module));
		log.debug("Task saved");
		return TaskResponse.of(saved);
	}

	/**
	 * Проверяет наличие TaskType в БД, если нет - создаёт.
	 */
	private TaskTypes ensureTaskTypeExists(Long taskTypeId) {
		Optional<TaskTypes> existing = taskTypeRepository.findById(taskTypeId);

		if (existing.isPresent()) {
			return existing.get();
		}

		// Если нет - создаём
		TaskTypes newTaskType = new TaskTypes();
		newTaskType.setId(taskTypeId);

		if (taskTypeId == 1) {
			newTaskType.setName("ONE_POSSIBLE_ANSWER");
		} else if (taskTypeId == 2) {
			newTaskType.setName("MULTIPLE");
		} else {
			throw new RuntimeException("Unknown task type id: " + taskTypeId);
		}

		log.info("TaskType с id={} не найден в БД, создаём новый: {}", taskTypeId,
				newTaskType.getName());
		return taskTypeRepository.save(newTaskType);
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
		task.setContent(updateTaskDto.getContentAsMap());
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

		return taskRepository.findByModule(module).stream().map((task) -> TaskResponse.of(task))
				.sorted((p1, p2) -> Long.compare(p1.getId(), p2.getId()))
				.collect(Collectors.toList());
	}
}
