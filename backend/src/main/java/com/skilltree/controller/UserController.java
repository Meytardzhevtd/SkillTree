package com.skilltree.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skilltree.Service.JwtService;
import com.skilltree.Service.UserService;
import com.skilltree.dto.courses.CourseSimpleDto;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {
	private final UserService userService;
	private final JwtService jwtService;

	public UserController(UserService userService, JwtService jwtService) {
		this.userService = userService;
		this.jwtService = jwtService;
	}

	@Operation(summary = "Получить курсы",
			description = "Получить курсы, которые проходит пользователь")
	@GetMapping("/courses")
	public ResponseEntity<List<CourseSimpleDto>> getAllCoursesByUserId(
			@RequestHeader("Authorization") String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(401).build();
		}

		String token = authHeader.substring(7);
		String email = jwtService.extractEmail(token);

		if (!jwtService.isTokenValid(token, email)) {
			return ResponseEntity.status(401).build();
		}

		Long userId = jwtService.extractUserId(token);

		return ResponseEntity.ok(userService.getCoursesForUser(userId));
	}
}
