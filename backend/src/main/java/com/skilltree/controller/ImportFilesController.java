package com.skilltree.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

import com.skilltree.Service.ImportCourseFromFileService;

@RestController
@RequestMapping("/api/import-files")
public class ImportFilesController {
	private final ImportCourseFromFileService importCourseFromFileService;

	public ImportFilesController(ImportCourseFromFileService importCourseFromFileService) {
		this.importCourseFromFileService = importCourseFromFileService;
	}

	@PostMapping("/json-course")
	public ResponseEntity<?> importCourse(@RequestParam("file") MultipartFile file) {
		try {
			Long courseId = importCourseFromFileService.importCourseFromJson(file);
			return ResponseEntity.ok(Map.of("courseId", courseId));
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
}
