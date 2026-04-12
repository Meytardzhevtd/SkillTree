package com.skilltree.Service;

import com.skilltree.dto.courses.CourseDto;
import com.skilltree.dto.courses.CourseSimpleDto;
import com.skilltree.exception.CourseNotFoundException;
import com.skilltree.exception.UserNotFoundException;
import com.skilltree.model.Courses;
import com.skilltree.model.Roles;
import com.skilltree.model.Users;
import com.skilltree.dto.courses.CreateCourseRequest;
import com.skilltree.repository.CourseRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.RolesRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CourseService {

	private final CourseRepository courseRepository;
	private final ModuleRepository moduleRepository;
	private final TaskRepository taskRepository;
	private final UserRepository userRepository;
	private final RolesRepository rolesRepository;

	public CourseService(CourseRepository courseRepository, ModuleRepository moduleRepository,
			TaskRepository taskRepository, UserRepository userRepository,
			RolesRepository rolesRepository) {
		this.courseRepository = courseRepository;
		this.moduleRepository = moduleRepository;
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
		this.rolesRepository = rolesRepository;
	}

	@Transactional
	public CourseDto createCourse(CreateCourseRequest request) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}

		String email = auth.getName();

		Courses course = new Courses();
		course.setName(request.getName());
		course.setDescription(request.getDescription());
		Courses savedCourse = courseRepository.save(course);

		Users user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException(1L));

		Roles role = new Roles();
		role.setCourse_role("admin");
		role.setCourse(savedCourse);
		role.setUser(user);
		rolesRepository.save(role);

		return new CourseDto(savedCourse);
	}

	public List<CourseDto> getAll() {
		return courseRepository.findAll().stream().map(CourseDto::new).toList();
	}

	public List<CourseSimpleDto> getAllCourseSimpleDto() {
		return courseRepository.findAll().stream().map(CourseSimpleDto::new)
				.collect(Collectors.toList());
	}

	public CourseDto getCourseDtoById(Long id) {
		Courses course = courseRepository.findById(id)
				.orElseThrow(() -> new CourseNotFoundException(id));
		return new CourseDto(course);
	}

	public List<CourseSimpleDto> getCoursesByUserAndRole(String role) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}
		String email = auth.getName();
		Users user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UserNotFoundException(1L));
		return courseRepository.findByUserIdAndRole(user.getId(), role).stream()
				.map(CourseSimpleDto::new).collect(Collectors.toList());
	}

	public List<CourseSimpleDto> getCoursesByUserId(Long userId) {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new UserNotFoundException(userId));
		return courseRepository.findByUser(user).stream().map(CourseSimpleDto::new)
				.collect(Collectors.toList());
	}

	public String getMyRoleInCourse(Long courseId) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			return null;
		}
		String email = auth.getName();
		Users user = userRepository.findByEmail(email).orElse(null);
		if (user == null)
			return null;

		if (rolesRepository.existsByCourseIdAndUserIdAndRole(courseId, user.getId(), "admin")) {
			return "admin";
		}
		if (rolesRepository.existsByCourseIdAndUserIdAndRole(courseId, user.getId(), "student")) {
			return "student";
		}
		return null;
	}
}
