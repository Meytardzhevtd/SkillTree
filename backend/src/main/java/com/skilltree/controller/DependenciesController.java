package com.skilltree.controller;

import com.skilltree.Service.DependenciesService;
import com.skilltree.dto.dependencies.DependencyConstructorDto;
import com.skilltree.dto.dependencies.DependencyTakeCourseDto;
import com.skilltree.dto.dependencies.UpdateDependencyDto;
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

	@PostMapping("{idMainModule}/{idBlockedModule}")
	public boolean makeDependent(@PathVariable Long idMainModule,
			@PathVariable Long idBlockedModule) {
		return dependenciesService.makeDependent(idMainModule, idBlockedModule);
	}

	@GetMapping("graph/takenCourse/{takenCourse}")
	public HashMap<Long, List<DependencyTakeCourseDto>> getGraphByModule(
			@PathVariable Long takenCourse) {
		return dependenciesService.getUserCourseGraph(takenCourse);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		dependenciesService.deleteDependence(id);
	}

	@GetMapping("graph/{idCourse}")
	public HashMap<Long, List<DependencyConstructorDto>> getGraphByCourse(
			@PathVariable Long idCourse) {
		return dependenciesService.getCourseGraph(idCourse);
	}

	@PutMapping("/update/{id}")
	public void update(@PathVariable Long id, @RequestBody UpdateDependencyDto dto) {
		dependenciesService.updateDependency(id, dto.getMainModuleId(), dto.getBlockedModuleId());
	}

}
