package com.skilltree.Service;

import com.skilltree.dto.courses.CourseSimpleDto;
import com.skilltree.dto.takeCourse.TakeCourseDto;
import com.skilltree.dto.takeCourse.TakenCourseInfo;

import com.skilltree.exception.UserNotFoundException;

import com.skilltree.model.Courses;
import com.skilltree.model.Module;
import com.skilltree.model.ProgressModule;
import com.skilltree.model.Roles;
import com.skilltree.model.TakenCourses;
import com.skilltree.model.Users;

import com.skilltree.repository.CourseRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.ProgressModuleRepository;
import com.skilltree.repository.RolesRepository;
import com.skilltree.repository.TakenCoursesRepository;
import com.skilltree.repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TakeCourseService {
	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final TakenCoursesRepository takenCoursesRepository;
	private final RolesRepository rolesRepository;
	private final ModuleRepository moduleRepository;
	private final ProgressModuleRepository progressModuleRepository;

	public TakeCourseService(UserRepository userRepository, CourseRepository courseRepository,
			TakenCoursesRepository takenCoursesRepository, RolesRepository rolesRepository,
			ModuleRepository moduleRepository, ProgressModuleRepository progressModuleRepository) {
		this.userRepository = userRepository;
		this.courseRepository = courseRepository;
		this.takenCoursesRepository = takenCoursesRepository;
		this.rolesRepository = rolesRepository;
		this.moduleRepository = moduleRepository;
		this.progressModuleRepository = progressModuleRepository;
	}

	public CourseSimpleDto takeCourse(TakeCourseDto request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}

		Users user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new UserNotFoundException(1L));
		Courses course = courseRepository.findById(request.getCourseId())
				.orElseThrow(() -> new RuntimeException("Course not found"));

		if (takenCoursesRepository.existsByCourseIdAndUserId(request.getCourseId(),
				request.getUserId())) {
			throw new RuntimeException("Course have already chosen");
		}

		TakenCourses takenCourses = new TakenCourses();
		takenCourses.setCourse(course);
		takenCourses.setUser(user);
		TakenCourses savedTakenCourse = takenCoursesRepository.save(takenCourses);

		Roles role = new Roles();
		role.setCourse_role("student");
		role.setCourse(course);
		role.setUser(user);
		rolesRepository.save(role);

		List<Module> modules = moduleRepository.findByCourseIdOrderById(course.getId());
		for (Module module : modules) {
			ProgressModule pm = new ProgressModule();
			pm.setModule(module);
			pm.setTaken_courses(savedTakenCourse);
			pm.setProgress(0.0f);
			progressModuleRepository.save(pm);
		}

		return new CourseSimpleDto(course);
	}

	@Transactional(readOnly = true)
	public List<TakenCourseInfo> getByUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}
		String email = auth.getName();
		Users user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
		return takenCoursesRepository.findByUserId(user.getId()).stream().map(TakenCourseInfo::new)
				.toList();
	}
}