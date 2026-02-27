package com.skilltree.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skilltree.Service.CourseService;
import com.skilltree.dto.CourseDto;
import com.skilltree.dto.RegCourse;
import com.skilltree.dto.UserIdRequest;
import com.skilltree.model.Course;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/course-manager")
public class CourseManagerController {
	private final CourseService courseService;

	public CourseManagerController(CourseService courseService) {
		this.courseService = courseService;
	}

	@GetMapping("courses")
	public ResponseEntity<List<CourseDto>> getCoursesByUserId(@RequestParam("userId") Long userId) {
		List<Course> entities = courseService.getCoursesByUserId(userId);
		List<CourseDto> courses = entities.stream().map(com.skilltree.dto.CourseDto::fromEntity)
				.collect(Collectors.toList());
		return ResponseEntity.ok(courses);
	}

	@PostMapping("create-course")
	public ResponseEntity<?> createCourse(@RequestBody RegCourse reques) {
		List<Course> courses = courseService.getCoursesByUserId(reques.getUserId());
		for (Course course : courses) {
			if (course.getName().equals(reques.getName())) {
				return ResponseEntity.badRequest().body(
						"Курс с таким названием уже существует (придумайте новое название, которого у вас нет)");
			}
		}
		courseService.createCourse(reques.getUserId(), reques.getName(), reques.getDescription());
		return ResponseEntity.ok().body("Курс " + reques.getName() + " создан");
	}

}
