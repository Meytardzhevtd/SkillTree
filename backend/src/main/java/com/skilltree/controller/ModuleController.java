package com.skilltree.controller;

import com.skilltree.Service.CourseService;
import com.skilltree.Service.ModuleService;
import com.skilltree.dto.module.ModuleDto;
import com.skilltree.dto.module.ModuleResponse;
import com.skilltree.dto.courses.CourseDto;
import com.skilltree.dto.courses.CreateCourseRequest;
import com.skilltree.dto.module.CreateModuleRequest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/module")
public class ModuleController {
	private final ModuleService moduleService;

	public ModuleController(ModuleService moduleService) {
		this.moduleService = moduleService;
	}

	@PostMapping
	public ModuleResponse create(@RequestBody CreateModuleRequest request) {
		return moduleService.createModule(request);
	}

	@GetMapping("/{id}")
	public ModuleResponse getModule(@PathVariable Long id) {
		return moduleService.getModule(id);
	}
	
}
