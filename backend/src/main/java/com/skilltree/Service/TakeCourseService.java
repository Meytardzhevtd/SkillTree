package com.skilltree.Service;
import com.skilltree.dto.courses.CourseSimpleDto;
import com.skilltree.dto.takeCourse.TakeCourseDto;
import com.skilltree.dto.takeCourse.TakenCourseInfo;
import com.skilltree.exception.UserNotFoundException;
import com.skilltree.model.Courses;
import com.skilltree.model.Roles;
import com.skilltree.model.TakenCourses;
import com.skilltree.model.Users;
import com.skilltree.repository.RolesRepository;
import com.skilltree.repository.TakenCoursesRepository;
import com.skilltree.repository.UserRepository;
import com.skilltree.repository.CourseRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class TakeCourseService {
	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final TakenCoursesRepository takenCoursesRepository;
	private final RolesRepository rolesRepository;

	public TakeCourseService(UserRepository userRepository, CourseRepository courseRepository,
			TakenCoursesRepository takenCoursesRepository, RolesRepository rolesRepository) {
		this.userRepository = userRepository;
		this.courseRepository = courseRepository;
		this.takenCoursesRepository = takenCoursesRepository;
		this.rolesRepository = rolesRepository;

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

		if (!takenCoursesRepository.existsByCourseIdAndUserId(request.getCourseId(),
				request.getUserId())) {
			TakenCourses takenCourses = new TakenCourses();
			takenCourses.setCourse(course);
			takenCourses.setUser(user);
			takenCoursesRepository.save(takenCourses);

			Roles role = new Roles();
			role.setCourse_role("student");
			role.setCourse(course);
			role.setUser(user);
			rolesRepository.save(role);
		} else {
			throw new RuntimeException("Course have already chosen");
		}

		return new CourseSimpleDto(request.getCourseId(), course.getName(),
				course.getDescription());
	}

	@Transactional
	public List<TakenCourseInfo> getByUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !auth.isAuthenticated()) {
			throw new RuntimeException("User not authenticated");
		}
		String email = auth.getName();
		Users user = userRepository.findByEmail(email)
				.orElseThrow(() -> new RuntimeException("User not found"));
		return takenCoursesRepository.findByUserId(user.getId()).stream()
				.map((takenCourse) -> new TakenCourseInfo(takenCourse)).toList();
	}

}
