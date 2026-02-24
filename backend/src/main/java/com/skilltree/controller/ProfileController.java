package com.skilltree.controller;

import com.skilltree.Service.UserService;
import com.skilltree.dto.ProfileResponse;
import com.skilltree.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
	private final UserService userService;

	public ProfileController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public ResponseEntity<?> me(Principal principal) {
		try {
			// После прохождения JWT-фильтра principal.getName() содержит email из токена
			// (subject).
			// Это безопаснее, чем принимать email как query-параметр от клиента.
			if (principal == null) {
				return ResponseEntity.status(401).body("Unauthorized");
			}

			String email = principal.getName();
			User user = userService.findByEmail(email);
			ProfileResponse response = new ProfileResponse(user.getId(), user.getUsername(),
					user.getEmail(), user.getRole());
			return ResponseEntity.ok(response);
		} catch (RuntimeException ex) {
			return ResponseEntity.badRequest().body(ex.getMessage());
		}
	}
}
