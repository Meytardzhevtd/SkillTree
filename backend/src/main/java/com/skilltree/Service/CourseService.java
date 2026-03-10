package com.skilltree.Service;

import com.skilltree.dto.courses.CourseDto;
import com.skilltree.dto.module.ModuleDto;
import com.skilltree.model.Courses;
import com.skilltree.dto.courses.CreateCourseRequest;
import com.skilltree.repository.CourseRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.UserRepository;

import jakarta.transaction.Transactional;
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

	public CourseService(CourseRepository courseRepository, ModuleRepository moduleRepository,
			TaskRepository taskRepository, UserRepository userRepository) {
		this.courseRepository = courseRepository;
		this.moduleRepository = moduleRepository;
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
	}

	public CourseDto createCourse(CreateCourseRequest request) {
		Courses course = new Courses();
		course.setName(request.getName());
		course.setDescription(request.getDescription());
		return new CourseDto (courseRepository.save(course));
	}

	@Transactional
	public CourseDto getCourseDtoById(Long id) {
		Optional<Courses> course = courseRepository.findById(id);
		if(course.isEmpty()){
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