package com.skilltree.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import com.skilltree.model.ProgressModule;
import com.skilltree.model.TakenCourses;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import com.skilltree.model.UserAnswers;
import com.skilltree.model.Users;

import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.ProgressModuleRepository;
import com.skilltree.repository.RolesRepository;
import com.skilltree.repository.TakenCoursesRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.TaskTypeRepository;
import com.skilltree.repository.UserAnswerRepository;
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
	private final ProgressModuleRepository progressModuleRepository;
	private final UserAnswerRepository userAnswerRepository;
	private final TakenCoursesRepository takenCoursesRepository;

	@Autowired
	public TaskService(TaskRepository taskRepository, TaskTypeRepository taskTypeRepository,
			ModuleRepository moduleRepository, RolesRepository rolesRepository,
			UserRepository userRepository, ProgressModuleRepository progressModuleRepository,
			UserAnswerRepository userAnswerRepository,
			TakenCoursesRepository takenCoursesRepository) {
		this.taskRepository = taskRepository;
		this.taskTypeRepository = taskTypeRepository;
		this.moduleRepository = moduleRepository;
		this.rolesRepository = rolesRepository;
		this.userRepository = userRepository;
		this.progressModuleRepository = progressModuleRepository;
		this.userAnswerRepository = userAnswerRepository;
		this.takenCoursesRepository = takenCoursesRepository;
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

		Task saved = taskRepository
				.save(createTaskDto.toEntity(taskType, module, createTaskDto.getScore()));
		log.info("Задание создано, id={}", saved.getId());

		recalculateAllProgressesForModule(module);

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

		Module module = task.getModule();
		taskRepository.delete(task);

		recalculateAllProgressesForModule(module);
	}

	public List<TaskResponse> getAllTasksByModule(Long moduleId, Long progressModuleId) {
		Module module = moduleRepository.findById(moduleId)
				.orElseThrow(() -> new ModuleNotFoundException(moduleId));

		List<Task> tasks = taskRepository.findByModule(module);

		if (progressModuleId == null) {
			return tasks.stream().map(TaskResponse::of)
					.sorted((p1, p2) -> Long.compare(p1.getId(), p2.getId()))
					.collect(Collectors.toList());
		}

		ProgressModule progressModule = progressModuleRepository.findById(progressModuleId)
				.orElse(null);

		Set<Long> solvedTaskIds = new HashSet<>();
		if (progressModule != null) {
			List<UserAnswers> answers = userAnswerRepository.findByProgressModule(progressModule);
			for (UserAnswers ua : answers) {
				if (ua.isCorrect()) {
					solvedTaskIds.add(ua.getTask().getId());
				}
			}
		}

		return tasks.stream().sorted((a, b) -> Long.compare(a.getId(), b.getId()))
				.map(t -> TaskResponse.of(t, solvedTaskIds.contains(t.getId())))
				.collect(Collectors.toList());
	}

	private void recalculateAllProgressesForModule(Module module) {
		long totalTasks = taskRepository.findByModule(module).size();

		List<ProgressModule> progressModules = progressModuleRepository
				.findByModuleId(module.getId());

		Set<Long> affectedTakenCourseIds = new HashSet<>();
		for (ProgressModule pm : progressModules) {
			if (totalTasks == 0) {
				pm.setProgress(0.0f);
			} else {
				long solved = userAnswerRepository.countDistinctCorrectTasksByProgressModule(pm);
				pm.setProgress((float) solved / totalTasks * 100);
			}
			progressModuleRepository.save(pm);
			affectedTakenCourseIds.add(pm.getTaken_courses().getId());

			log.info("Пересчёт прогресса модуля id={} для takenCourseId={}: {}%", module.getId(),
					pm.getTaken_courses().getId(), pm.getProgress());
		}

		for (Long takenCourseId : affectedTakenCourseIds) {
			TakenCourses takenCourses = takenCoursesRepository.findById(takenCourseId).orElse(null);
			if (takenCourses != null) {
				recalculateCourseProgress(takenCourses);
			}
		}
	}

	private void recalculateCourseProgress(TakenCourses takenCourses) {
		List<ProgressModule> allModules = takenCourses.getProgress_modules();

		if (allModules.isEmpty())
			return;

		long totalTasksInCourse = 0;
		long solvedTasksInCourse = 0;

		for (ProgressModule pm : allModules) {
			long totalInModule = taskRepository.findByModule(pm.getModule()).size();
			long solvedInModule = userAnswerRepository
					.countDistinctCorrectTasksByProgressModule(pm);
			totalTasksInCourse += totalInModule;
			solvedTasksInCourse += solvedInModule;
		}

		float courseProgress = totalTasksInCourse == 0
				? 0
				: (float) solvedTasksInCourse / totalTasksInCourse * 100;

		takenCourses.setProgress(courseProgress);
		takenCoursesRepository.save(takenCourses);

		log.info("Прогресс курса takenCourseId={} обновлён: {}/{}={}%", takenCourses.getId(),
				solvedTasksInCourse, totalTasksInCourse, courseProgress);
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
