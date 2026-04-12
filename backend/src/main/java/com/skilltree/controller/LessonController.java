package com.skilltree.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skilltree.Service.LessonService;
import com.skilltree.dto.lessons.CreateLessonRequest;
import com.skilltree.dto.lessons.LessonResponse;
import com.skilltree.repository.LessonRepository;

@RestController
@RequestMapping("api/lessons")
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
        return null;
    }

    @GetMapping("all-lessons-by-module/{moduleId}")
    public ResponseEntity<List<LessonResponse>> getAllByModuleId(@PathVariable Long moduleId) {
        return null;
    }
}
