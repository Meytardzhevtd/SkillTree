package com.skilltree.Service;

import com.skilltree.dto.module.ModuleDto;
import com.skilltree.dto.module.ModuleResponse;
import com.skilltree.dto.module.ModuleSimpleDto;
import com.skilltree.dto.tasks.TaskSimpleDto;
import com.skilltree.exception.CourseNotFoundException;
import com.skilltree.exception.ModuleIsNotAvailable;
import com.skilltree.exception.ModuleNotFoundException;
import com.skilltree.exception.UserNotFoundException;
import com.skilltree.model.Courses;
import com.skilltree.model.Module;
import com.skilltree.model.UserAnswers;
import com.skilltree.model.Users;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.dto.module.CreateModuleRequest;

import com.skilltree.repository.CourseRepository;
import com.skilltree.repository.DependencyRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.ProgressModuleRepository;
import com.skilltree.repository.UserRepository;

@Transactional(readOnly = true)
@Service
public class ModuleService {
	private final ModuleRepository moduleRepository;
	private final CourseRepository courseRepository;
	private final DependencyRepository dependencyRepository;
	private final ProgressModuleRepository progressModuleRepository;
	private final UserRepository userRepository;

	@Autowired
	public ModuleService(ModuleRepository moduleRepository, CourseRepository courseRepository,
			DependencyRepository dependencyRepository,
			ProgressModuleRepository progressModuleRepository, UserRepository userRepository) {
		this.moduleRepository = moduleRepository;
		this.courseRepository = courseRepository;
		this.dependencyRepository = dependencyRepository;
		this.progressModuleRepository = progressModuleRepository;
		this.userRepository = userRepository;
	}

	@Transactional
	public ModuleResponse createModule(CreateModuleRequest request) {
		Courses course = courseRepository.findById(request.getCourseId())
				.orElseThrow(() -> new CourseNotFoundException(request.getCourseId()));

		Module module = new Module(null, course, request.getName(), request.getCan_be_open());
		Module saved = moduleRepository.save(module);
		return new ModuleResponse(saved.getId(), saved.getName(), new ArrayList<TaskSimpleDto>());
	}

	public ModuleResponse getModule(Long moduleId) {
		Module module = moduleRepository.findById(moduleId)
				.orElseThrow(() -> new ModuleNotFoundException(moduleId));
		if (module.getCan_be_open()) {
			Optional<Long> currentUserId = getCurrentUserId();
			List<TaskSimpleDto> tasks = module.getTasks().stream()
					.map(task -> new TaskSimpleDto(task.getId(),
							isTaskCompletedByUser(task.getUser_answers(), currentUserId)))
					.toList();

			return new ModuleResponse(moduleId, module.getName(), tasks);
		} else {
			throw new ModuleIsNotAvailable(moduleId);
		}
	}

	private Optional<Long> getCurrentUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return Optional.empty();
		}

		return userRepository.findByEmail(auth.getName()).map(Users::getId);
	}

	private boolean isTaskCompletedByUser(List<UserAnswers> answers, Optional<Long> currentUserId) {
		if (currentUserId.isEmpty()) {
			return false;
		}
		Long userId = currentUserId.get();
		return answers.stream().anyMatch(answer -> isAnswerOfUser(answer, userId));
	}

	private boolean isAnswerOfUser(UserAnswers answer, Long userId) {
		if (answer.getProgress_module() == null) {
			return false;
		}
		if (answer.getProgress_module().getTaken_courses() == null) {
			return false;
		}
		Users answerUser = answer.getProgress_module().getTaken_courses().getUser();
		return answerUser != null && userId.equals(answerUser.getId());
	}

	public List<ModuleSimpleDto> getListModulesByCourseId(Long courseId) {

		Courses course = courseRepository.findById(courseId)
				.orElseThrow(() -> new CourseNotFoundException(courseId));

		return course.getModules().stream().map((module) -> new ModuleSimpleDto(module.getId(),
				module.getName(), module.getCan_be_open())).toList();
	}

	@Transactional
	public void deleteModule(Long moduleId) {
		Module module = moduleRepository.findById(moduleId)
				.orElseThrow(() -> new ModuleNotFoundException(moduleId));
		moduleRepository.delete(module);
	}

}
