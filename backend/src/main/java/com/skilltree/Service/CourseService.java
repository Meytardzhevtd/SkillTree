package com.skilltree.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skilltree.model.Course;
import com.skilltree.repository.CourseRepository;

@Service
public class CourseService {
	private final CourseRepository courseRepository;

	@Autowired
	public CourseService(CourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	public void createCourse(Long userId, String name, String description) {
		courseRepository.save(new Course(null, userId, name, description));
	}

}
