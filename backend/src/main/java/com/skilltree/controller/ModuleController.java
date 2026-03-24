package com.skilltree.controller;

import com.skilltree.Service.ModuleService;
import com.skilltree.dto.module.ModuleResponse;
import com.skilltree.dto.module.ModuleSimpleDto;

import io.swagger.v3.oas.annotations.Operation;

import com.skilltree.dto.module.CreateModuleRequest;

import java.util.List;

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

	@Operation(summary = "создать модуль",
			description = "Создается пустой модуль c названием <name>, "
					+ "привязывается к курсу по course_id, при необходимости блокируется для прохождения. "
					+ "Тогда надо будет указывать")
	@PostMapping
	public ModuleResponse create(@RequestBody CreateModuleRequest request) {
		return moduleService.createModule(request);
	}

	@GetMapping("/{id}")
	public ModuleResponse getModule(@PathVariable Long id) {
		return moduleService.getModule(id);
	}

	@GetMapping("/courses/{id}")
	public List<ModuleSimpleDto> getListModulesByCourseId(@PathVariable Long courseId) {
		return moduleService.getListModulesByCourseId(courseId);
	}
}
