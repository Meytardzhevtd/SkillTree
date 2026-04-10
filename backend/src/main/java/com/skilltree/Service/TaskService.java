package com.skilltree.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.skilltree.dto.tasks.CreateTaskDto;
import com.skilltree.dto.tasks.TaskResponse;
import com.skilltree.dto.tasks.UpdateTaskDto;
import com.skilltree.exception.ModuleNotFoundException;
import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.exception.TaskTypesNotFound;
import com.skilltree.model.Module;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import com.skilltree.model.Users;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.RolesRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.TaskTypeRepository;
import com.skilltree.repository.UserRepository;

@Transactional(readOnly = true)
@Service
public class TaskService {
	private static final Logger log = LoggerFactory.getLogger(TaskService.class);

	private final TaskRepository taskRepository;
	private final TaskTypeRepository taskTypeRepository;
	private final ModuleRepository moduleRepository;
	private final RolesRepository rolesRepository;
	private final UserRepository userRepository;

	@Autowired
	public TaskService(TaskRepository taskRepository, TaskTypeRepository taskTypeRepository,
			ModuleRepository moduleRepository, RolesRepository rolesRepository,
			UserRepository userRepository) {
		this.taskRepository = taskRepository;
		this.taskTypeRepository = taskTypeRepository;
		this.moduleRepository = moduleRepository;
		this.rolesRepository = rolesRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public TaskResponse create(CreateTaskDto createTaskDto) {
		log.info("Создание задания, taskTypeId={}, moduleId={}", createTaskDto.getTaskTypeId(),
				createTaskDto.getModuleId());

		TaskTypes taskType = taskTypeRepository.findById(createTaskDto.getTaskTypeId())
				.orElseThrow(() -> new TaskTypesNotFound(createTaskDto.getTaskTypeId()));

		Module module = moduleRepository.findById(createTaskDto.getModuleId())
				.orElseThrow(() -> new ModuleNotFoundException(createTaskDto.getModuleId()));

		checkAdminAccess(module.getCourse().getId());

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

		checkAdminAccess(task.getModule().getCourse().getId());

		TaskTypes taskType = taskTypeRepository.findById(updateTaskDto.getTaskTypeId())
				.orElseThrow(() -> new TaskTypesNotFound(updateTaskDto.getTaskTypeId()));

		task.setTask_type(taskType);
		task.setContent(updateTaskDto.getContent());

		Task saved = taskRepository.save(task);
		return TaskResponse.of(saved);
	}

	@Transactional
	public void delete(Long id) {
		Task task = taskRepository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));

		checkAdminAccess(task.getModule().getCourse().getId());

		taskRepository.delete(task);
	}

	public List<TaskResponse> getAllTasksByModule(Long moduleId) {
		Module module = moduleRepository.findById(moduleId)
				.orElseThrow(() -> new ModuleNotFoundException(moduleId));

		return taskRepository.findByModule(module).stream().map(TaskResponse::of)
				.sorted((p1, p2) -> Long.compare(p1.getId(), p2.getId()))
				.collect(Collectors.toList());
	}

	private void checkAdminAccess(Long courseId) {
		Long userId = getCurrentUserId()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
						"Пользователь не авторизован"));

		boolean isAdmin = rolesRepository.existsByCourseIdAndUserIdAndRole(courseId, userId,
				"admin");
		if (!isAdmin) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN,
					"У вас нет прав для редактирования этого курса");
		}
	}

	private Optional<Long> getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return Optional.empty();
		}
		return userRepository.findByEmail(auth.getName()).map(Users::getId);
	}
}
