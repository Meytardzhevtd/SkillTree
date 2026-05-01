package com.skilltree.controller;

import com.skilltree.Service.DependenciesService;
import com.skilltree.dto.dependencies.DependencyConstructorDto;
import com.skilltree.dto.dependencies.DependencyTakeCourseDto;
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

	@GetMapping("graph/takenCourse/{takenCourse}")
	public HashMap<Long, List<DependencyTakeCourseDto>> getGraphByModule(
			@PathVariable Long takenCourse) {
		return dependenciesService.getGraphOfModules(takenCourse);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		dependenciesService.deleteDependence(id);
	}

	@GetMapping("graph/{idCourse}")
	public HashMap<Long, List<DependencyConstructorDto>> getGraphByCourse(
			@PathVariable Long idCourse) {
		return dependenciesService.getAllGraph(idCourse);
	}

}
