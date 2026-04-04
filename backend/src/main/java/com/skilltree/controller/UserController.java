package com.skilltree.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skilltree.Service.UserService;
import com.skilltree.dto.courses.CourseSimpleDto;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/user")
public class UserController {
	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@Operation(summary = "Получить курсы",
			description = "Получить курсы, которые проходит пользователь")
	@GetMapping("/{id}/courses")
	public ResponseEntity<List<CourseSimpleDto>> getAllCoursesByUserId(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getCoursesForUser(id));
	}
}
