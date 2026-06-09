package com.skilltree.controller;

import java.util.List;

import com.skilltree.dto.lessons.UpdateLesson;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.skilltree.Service.LessonService;
import com.skilltree.dto.lessons.CreateLessonRequest;
import com.skilltree.dto.lessons.LessonResponse;
import com.skilltree.repository.LessonRepository;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {
	private final LessonService lessonService;

	public LessonController(LessonService lessonService) {
		this.lessonService = lessonService;
	}

	@PostMapping
	public ResponseEntity<LessonResponse> create(@RequestBody CreateLessonRequest request) {
		return ResponseEntity.ok(lessonService.createLesson(request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<LessonResponse> get(@PathVariable Long id) {
		return ResponseEntity.ok(lessonService.get(id));
	}

	@GetMapping("/module/{moduleId}")
	public ResponseEntity<List<LessonResponse>> getAllByModuleId(@PathVariable Long moduleId) {
		return ResponseEntity.ok(lessonService.getLessons(moduleId));
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		lessonService.delete(id);
	}

	@PutMapping("/{id}")
	public ResponseEntity<LessonResponse> updateLesson(@PathVariable Long id,
			@RequestBody UpdateLesson request) {
		return ResponseEntity.ok(lessonService.updateLesson(id, request));
	}
}
