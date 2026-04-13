package com.skilltree.controller;

import com.skilltree.Service.DependenciesService;
import com.skilltree.dto.dependencies.DependencyDto;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("api/dependencies")
public class DependenciesController {
	private final DependenciesService dependenciesService;

	public DependenciesController(DependenciesService dependenciesService) {
		this.dependenciesService = dependenciesService;
	}

	@PostMapping("{idModuleMain}/{idModuleDependent}")
	public boolean makeDependent(@PathVariable Long idModuleMain,
			@PathVariable Long idModuleDependent) {
		return dependenciesService.makeDependent(idModuleMain, idModuleDependent);
	}

	@GetMapping("graph/{takenCourse}/{idModuleRoot}")
	public HashMap<Long, List<DependencyDto>> getGraphByModule(@PathVariable Long takenCourse,
			@PathVariable Long idModuleRoot) {
		return dependenciesService.getGraphOfModules(takenCourse, idModuleRoot);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		dependenciesService.deleteDependence(id);
	}
}
