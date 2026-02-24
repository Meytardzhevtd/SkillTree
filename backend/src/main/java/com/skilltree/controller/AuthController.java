package com.skilltree.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skilltree.dto.RegisterRequest;
import com.skilltree.dto.LoginRequest;
import com.skilltree.Service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    public AuthController(UserService userService) {
        this.userService = userService;
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
            log.info("Login success: email={}", request.getEmail());
            return ResponseEntity.ok("Login successful");
        } else {
            log.warn("Login failed: email={}", request.getEmail());
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }
}
