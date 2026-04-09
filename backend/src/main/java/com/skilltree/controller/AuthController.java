package com.skilltree.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skilltree.dto.*;
import com.skilltree.model.Users;
import com.skilltree.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import com.skilltree.Service.UserService;
import com.skilltree.Service.JwtService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final UserService userService;
	private final JwtService jwtService;
	private static final Logger log = LoggerFactory.getLogger(AuthController.class);

	private final UserRepository userRepository;

	public AuthController(UserService userService, JwtService jwtService,
			UserRepository userRepository) {
		this.userService = userService;
		this.jwtService = jwtService;
		this.userRepository = userRepository;
	}

	@PostMapping("register")
	// TODO: добавить @Valid перед @RequestBody
	public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
		log.info("Register attempt: email={}", request.getEmail());
		try {
			userService.register(request);
			log.info("Register success: email={}", request.getEmail());
			return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
		} catch (RuntimeException ex) {
			log.warn("Register failed: email={}, reason={}", request.getEmail(), ex.getMessage());
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}

	@PostMapping("login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		log.info("Login attempt: email={}", request.getEmail());
		boolean success = userService.login(request);
		if (success) {
			Users user = userRepository.findByEmail(request.getEmail())
					.orElseThrow(() -> new RuntimeException("TODO: File AuthController"));

			String token = jwtService.generateToken(request.getEmail(), user.getId());

			// var user = userService.findByEmail(request.getEmail());

			AuthResponse.UserInfo userInfo = new AuthResponse.UserInfo(user.getId(),
					user.getUsername(), user.getEmail(), user.getRole().name());

			log.info("Login success: email={}", request.getEmail());
			return ResponseEntity.ok(new AuthResponse(token, "Bearer", userInfo));
		} else {
			log.warn("Login failed: email={}", request.getEmail());
			return ResponseEntity.badRequest().body("Invalid credentials");
		}
	}

	@GetMapping("/me")
	public ResponseEntity<?> getMe(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Missing or invalid Authorization header");
		}

		String token = authHeader.substring(7);

		try {
			String email = jwtService.extractEmail(token);

			if (!jwtService.isTokenValid(token, email)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
						.body("Invalid or expired token");
			}

			Long userId = jwtService.extractUserId(token);

			return ResponseEntity.ok(userId);

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid JWT token");
		}
	}
}
