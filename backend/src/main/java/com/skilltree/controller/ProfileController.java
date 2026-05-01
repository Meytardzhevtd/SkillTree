package com.skilltree.controller;

import com.skilltree.Service.UserService;
import com.skilltree.Service.UserTaskScoresService;
import com.skilltree.dto.ProfileResponse;
import com.skilltree.dto.UpdateProfileRequest;
import com.skilltree.model.Users;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
	private final UserService userService;
	private final UserTaskScoresService scoresService;

	public ProfileController(UserService userService, UserTaskScoresService scoresService) {
		this.userService = userService;
		this.scoresService = scoresService;
	}

	@GetMapping("/me")
	public ResponseEntity<?> me(Principal principal) {
		try {
			if (principal == null) {
				return ResponseEntity.status(401).body("Unauthorized");
			}

			String email = principal.getName();
			Users user = userService.findByEmail(email);
			Integer totalScore = scoresService.getTotalScore(user.getId());
			ProfileResponse response = new ProfileResponse(user.getId(), user.getUsername(),
					user.getEmail(), user.getRole(), totalScore);
			return ResponseEntity.ok(response);
		} catch (RuntimeException ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}

	@PutMapping("/me")
	public ResponseEntity<?> updateMe(Principal principal,
			@RequestBody UpdateProfileRequest request) {
		try {
			if (principal == null) {
				return ResponseEntity.status(401).body("Unauthorized");
			}

			String email = principal.getName();
			Users updatedUser = userService.updateUsernameByEmail(email, request.getUsername());
			Integer totalScore = scoresService.getTotalScore(updatedUser.getId());
			ProfileResponse response = new ProfileResponse(updatedUser.getId(),
					updatedUser.getUsername(), updatedUser.getEmail(), updatedUser.getRole(),
					totalScore);
			return ResponseEntity.ok(response);
		} catch (RuntimeException ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}
}
