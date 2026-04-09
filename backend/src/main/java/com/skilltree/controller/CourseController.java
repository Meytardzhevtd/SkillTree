package com.skilltree.controller;

import com.skilltree.Service.CourseService;
import com.skilltree.dto.courses.CourseDto;
import com.skilltree.dto.courses.CourseSimpleDto;
import com.skilltree.dto.courses.CreateCourseRequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {
	private final CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@PostMapping
	public CourseDto create(@RequestBody CreateCourseRequest request) {
		return courseService.createCourse(request);
	}

	@GetMapping("/{id}")
	public CourseDto getById(@PathVariable Long id) {
		return courseService.getCourseDtoById(id);
	}

	@GetMapping("/all")
	public ResponseEntity<List<CourseSimpleDto>> getAll() {
		return ResponseEntity.ok(courseService.getAllCourseSimpleDto());
	}

	@GetMapping("/my/{role}")
	public List<CourseSimpleDto> getMyCoursesByRole(@PathVariable String role) {
		return courseService.getCoursesByUserAndRole(role);
	}

}
