package com.skilltree.Service;

import java.util.List;
import com.skilltree.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skilltree.model.Course;
import com.skilltree.repository.CourseRepository;

@Service
public class CourseService {

	private final UserRepository userRepository;
	private final CourseRepository courseRepository;

	@Autowired
	public CourseService(CourseRepository courseRepository, UserRepository userRepository) {
		this.courseRepository = courseRepository;
		this.userRepository = userRepository;
	}

	public void createCourse(Long userId, String name, String description) {
		courseRepository.save(new Course(null, userId, name, description));
	}

	public List<Course> getCoursesByUserId(Long userId) {
		return courseRepository.findByUserId(userId);
	}

}
