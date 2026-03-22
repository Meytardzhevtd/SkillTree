package com.skilltree.Service;

import com.skilltree.dto.courses.CourseDto;
import com.skilltree.dto.module.ModuleDto;
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

import jakarta.transaction.Transactional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
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

		role.setCourse_role("base_user");
		role.setCourse(savedCourse);
		role.setUser(user);

		Roles savedRole = rolesRepository.save(role);

		return new CourseDto(savedCourse);

	}

	@Transactional
	public List<CourseDto> getAll() {
		return courseRepository.findAll().stream().map((course) -> new CourseDto(course)).toList();
	}

	@Transactional
	public CourseDto getCourseDtoById(Long id) {
		Optional<Courses> course = courseRepository.findById(id);
		if (course.isEmpty()) {
			throw new RuntimeException("Course not found");
		}
		return new CourseDto(course.get());
	}

	@Transactional
	public List<CourseDto> getCoursesByUserAndRole(Long userId, String role) {
		List<Courses> coursesUser = courseRepository.findByUserIdAndRole(userId, role);
		return coursesUser.stream().map(course -> new CourseDto(course))
				.collect(Collectors.toList());
	}

}