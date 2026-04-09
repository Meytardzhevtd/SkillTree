package com.skilltree.controller;

import com.skilltree.Service.ModuleService;
import com.skilltree.dto.module.CreateModuleRequest;
import com.skilltree.dto.module.ModuleResponse;
import com.skilltree.dto.module.ModuleSimpleDto;
import com.skilltree.dto.module.StartModuleResponse;

import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/module")
public class ModuleController {
	private final ModuleService moduleService;

	public ModuleController(ModuleService moduleService) {
		this.moduleService = moduleService;
	}

	@Operation(summary = "создать модуль",
			description = "Создается пустой модуль c названием, "
					+ "привязывается к курсу по course_id")
	@PostMapping
	public ModuleResponse create(@RequestBody CreateModuleRequest request) {
		return moduleService.createModule(request);
	}

	@GetMapping("/{id}")
	public ModuleResponse getModule(@PathVariable Long id) {
		return moduleService.getModule(id);
	}

	@Operation(summary = "Получить список модулей",
			description = "Возвращается список модулей по id курса")
	@GetMapping("/courses/{id}")
	public List<ModuleSimpleDto> getListModulesByCourseId(@PathVariable Long id) {
		return moduleService.getListModulesByCourseId(id);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		moduleService.deleteModule(id);
	}

	@PostMapping("/{moduleId}/start")
	public StartModuleResponse startModule(@PathVariable Long moduleId,
			@RequestParam Long takenCourseId) {
		return moduleService.startModule(moduleId, takenCourseId);
	}
}
