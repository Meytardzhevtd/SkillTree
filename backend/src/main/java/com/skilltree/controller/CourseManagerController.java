package com.skilltree.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import com.skilltree.Service.CourseService;
import com.skilltree.dto.CreateCourseRequest;
import com.skilltree.dto.CreateModuleRequest;
import com.skilltree.dto.CreateTaskRequest;
import com.skilltree.dto.CourseDto;
import com.skilltree.model.Course;
import com.skilltree.model.Module;
import com.skilltree.model.Task;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course-manager")
public class CourseManagerController {

	private final CourseService courseService;

	public CourseManagerController(CourseService courseService) {
		this.courseService = courseService;
	}

	@GetMapping("/courses")
	public ResponseEntity<List<CourseDto>> getCoursesByUserId(@RequestParam("userId") Long userId) {
		List<Course> entities = courseService.getCoursesByUserId(userId);
		List<CourseDto> courses = entities.stream().map(CourseDto::fromEntity)
				.collect(Collectors.toList());
		return ResponseEntity.ok(courses);
	}

	@PostMapping("/create-course")
	public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest request) {
		courseService.createCourse(request.getUserId(), request.getName(),
				request.getDescription());
		return ResponseEntity.ok("Курс " + request.getName() + " создан");
	}

	@PostMapping("/create-full-course")
	public ResponseEntity<?> createFullCourse(@RequestBody CreateCourseRequest request) {
		courseService.createFullCourse(request);
		return ResponseEntity.ok("Полный курс успешно создан");
	}

	@PostMapping("/add-module")
	public ResponseEntity<?> addModule(@RequestBody CreateModuleRequest request,
			@RequestParam Long courseId) {
		Module module = courseService.addModuleToCourse(courseId, request.getName());
		return ResponseEntity.ok("Модуль " + module.getName() + " добавлен");
	}

	@PostMapping("/add-task")
	public ResponseEntity<?> addTask(@RequestBody CreateTaskRequest request,
			@RequestParam Long moduleId) {
		Task task = courseService.addTaskToModule(moduleId, request.getContent());
		return ResponseEntity.ok("Задача добавлена в модуль");
	}

	@GetMapping("/course/{id}")
	public ResponseEntity<CourseDto> getCourse(@PathVariable Long id) {
		Course course = courseService.getCourseById(id);
		return ResponseEntity.ok(CourseDto.fromEntity(course));
	}
}