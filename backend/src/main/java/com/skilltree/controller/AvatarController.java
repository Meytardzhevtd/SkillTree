package com.skilltree.controller;

import com.skilltree.model.Avatar;
import com.skilltree.model.Users;
import com.skilltree.repository.AvatarRepository;
import com.skilltree.repository.UserRepository;
import com.skilltree.Service.MinioService;
import com.skilltree.Service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/avatar")
public class AvatarController {

	private final MinioService minioService;
	private final UserRepository userRepository;
	private final AvatarRepository avatarRepository;
	private final JwtService jwtService;

	public AvatarController(MinioService minioService, UserRepository userRepository,
			AvatarRepository avatarRepository, JwtService jwtService) {
		this.minioService = minioService;
		this.userRepository = userRepository;
		this.avatarRepository = avatarRepository;
		this.jwtService = jwtService;
	}

	@PostMapping("/upload")
	public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
			@RequestHeader("Authorization") String authHeader) {
		try {
			// Извлекаем токен из заголовка
			String token = authHeader.substring(7);
			String email = jwtService.extractEmail(token);

			Users user = userRepository.findByEmail(email)
					.orElseThrow(() -> new RuntimeException("User not found"));

			String fileName = minioService.uploadFile(file, "avatars/" + user.getId());
			String fileUrl = minioService.getFileUrl(fileName);

			Avatar avatar = new Avatar();
			avatar.setUser(user);
			avatar.setFileUrl(fileUrl);
			avatar.setCreatedAt(LocalDateTime.now());
			avatarRepository.save(avatar);

			return ResponseEntity.ok("Avatar uploaded: " + fileUrl);
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error: " + e.getMessage());
		}
	}

	@GetMapping("/me")
	public ResponseEntity<?> getMyAvatar(@RequestHeader("Authorization") String authHeader) {
		try {
			String token = authHeader.substring(7);
			String email = jwtService.extractEmail(token);

			Users user = userRepository.findByEmail(email)
					.orElseThrow(() -> new RuntimeException("User not found"));

			return avatarRepository.findTopByUserOrderByCreatedAtDesc(user)
					.map(avatar -> ResponseEntity.ok(avatar.getFileUrl()))
					.orElse(ResponseEntity.ok(""));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body("Error: " + e.getMessage());
		}
	}
}