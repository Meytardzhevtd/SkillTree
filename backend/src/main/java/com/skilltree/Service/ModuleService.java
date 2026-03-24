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
import com.skilltree.model.Users;

import java.util.ArrayList;
import java.util.List;

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

	private final JwtService jwtService;

	@Autowired
	public ModuleService(ModuleRepository moduleRepository, CourseRepository courseRepository,
			DependencyRepository dependencyRepository,
			ProgressModuleRepository progressModuleRepository, JwtService jwtService,
			UserRepository userRepository) {
		this.moduleRepository = moduleRepository;
		this.courseRepository = courseRepository;
		this.dependencyRepository = dependencyRepository;
		this.progressModuleRepository = progressModuleRepository;
		this.jwtService = jwtService;
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
			// TODO: надо сделать нормальную проверку решена ли таска или нет (сейчас всегда
			// false)
			List<TaskSimpleDto> tasks = module.getTasks().stream()
					.map(task -> new TaskSimpleDto(task.getId(), false)).toList();

			return new ModuleResponse(moduleId, module.getName(), tasks);
		} else {
			throw new ModuleIsNotAvailable(moduleId);
		}
	}

	public List<ModuleSimpleDto> getListModulesByCourseId(Long courseId) {
		/**
		 * Это метод как будто нафиг нам не нужен и я зря сидел и писал его долго :(
		 */
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
