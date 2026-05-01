package com.skilltree.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skilltree.Service.UserTaskScoresService;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {
	private final UserTaskScoresService scoresService;

	public ScoreController(UserTaskScoresService scoresService) {
		this.scoresService = scoresService;
	}

	@PutMapping("/change/{taskId}/{newScore}")
	public ResponseEntity<?> change(@PathVariable Long taskId, @PathVariable Integer newScore) {
		scoresService.changeScore(taskId, newScore);
		return ResponseEntity.ok("Score updated");
	}

	@GetMapping("/{userId}")
	public Integer getTotalScore(@PathVariable Long userId) {
		return scoresService.getTotalScore(userId);
	}
}
