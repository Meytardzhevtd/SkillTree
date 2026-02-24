package com.skilltree.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skilltree.dto.*;
import com.skilltree.Service.UserService;
import com.skilltree.Service.JwtService;
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

	public AuthController(UserService userService, JwtService jwtService) {
		this.userService = userService;
		this.jwtService = jwtService;
	}

	@PostMapping("register")
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
			String token = jwtService.generateToken(request.getEmail());
			log.info("Login success: email={}", request.getEmail());
			return ResponseEntity.ok(new AuthResponse(token, "Bearer"));
		} else {
			log.warn("Login failed: email={}", request.getEmail());
			return ResponseEntity.badRequest().body("Invalid credentials");
		}
	}
}
