package com.skilltree.Service;

import com.skilltree.dto.module.ModuleSimpleDto;
import com.skilltree.model.Dependencies;
import com.skilltree.model.ProgressModule;
import com.skilltree.repository.DependencyRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.ProgressModuleRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class DependenciesService {
	private final ModuleRepository moduleRepository;
	private final DependencyRepository dependencyRepository;
	private final ProgressModuleRepository progressModuleRepository;
	private final HashMap<Long, List<Long>> graph = new HashMap<>();

	public DependenciesService(ModuleRepository moduleRepository,
			DependencyRepository dependencyRepository,
			ProgressModuleRepository progressModuleRepository) {
		this.moduleRepository = moduleRepository;
		this.dependencyRepository = dependencyRepository;
		this.progressModuleRepository = progressModuleRepository;
	}

	private void dfs(Long node) {
		// граф должен быть деревом, поэтому без проверки на то, что
		// снова зайдем в одну и ту же вершину во время обхода
		List<Long> list = dependencyRepository.findByModuleId(node).stream()
				.map(dependencies -> dependencies.getBlock_module().getId()).toList();
		graph.put(node, list);
		for (Long next : list) {
			dfs(next);
		}
	}

	private void makeGraph(Long root) {
		graph.clear();
		dfs(root);
	}

	private boolean checkForCycles(Long node, Long neverReach) {
		boolean returnValue = false;
		for (Long next : graph.get(node)) {
			if (next.equals(neverReach)) {
				return true;
			}
			returnValue = checkForCycles(next, neverReach);
			if (returnValue) {
				break;
			}
		}
		return returnValue;
	}

	private boolean checkForModule(Long node, Long search) {
		for (Long next : graph.get(node)) {
			if (next.equals(search)) {
				return true;
			}
		}
		return false;
	}

	@Transactional
	public boolean makeDependent(Long idModuleMain, Long idModuleDependent) {
		makeGraph(idModuleMain);

		if (!checkForCycles(idModuleMain, idModuleMain)) {
			if (graph.containsKey(idModuleMain)) {
				if (!checkForModule(idModuleMain, idModuleDependent)) {
					graph.get(idModuleMain).add(idModuleDependent);
				} else {
					// TODO: передавать соответствующее сообщение об ошибке
					return false;
				}
			} else {
				graph.put(idModuleMain, new ArrayList<>(List.of(idModuleDependent)));
			}
		} else {
			// TODO: передавать соответствующее сообщение об ошибке
			return false;
		}
		Dependencies dep = new Dependencies();
		dep.setModule(moduleRepository.getReferenceById(idModuleMain));
		dep.setBlock_module(moduleRepository.getReferenceById(idModuleDependent));
		dependencyRepository.save(dep);
		return true;
	}

	private boolean checkIsOpen(Long takenCourseId, Long moduleId) {
		List<Long> mainModules = dependencyRepository.findByBlockModuleId(moduleId).stream()
				.map(id -> id.getModule().getId()).toList();
		for (Long mainModuleId : mainModules) {
			ProgressModule progress = progressModuleRepository
					.findByModuleIdAndTakenCoursesId(mainModuleId, takenCourseId).orElse(null);
			if (progress == null || progress.getProgress() != 100.0f) {
				return false;
			}
		}
		return true;
	}

	private void dfs2(Long takenCourseId, Long node,
			HashMap<Long, List<ModuleSimpleDto>> newGraph) {
		for (Long next : graph.get(node)) {
			dfs2(takenCourseId, next, newGraph);
		}
		List<ModuleSimpleDto> list = graph.get(node).stream()
				.map(moduleId -> new ModuleSimpleDto(moduleId,
						moduleRepository.getReferenceById(moduleId).getName(),
						checkIsOpen(takenCourseId, moduleId)))
				.toList();
		newGraph.put(node, list);
	}

	public HashMap<Long, List<ModuleSimpleDto>> getGraphOfModules(Long takenCourseId, Long root) {
		makeGraph(root);
		HashMap<Long, List<ModuleSimpleDto>> newGraph = new HashMap<>();
		dfs2(takenCourseId, root, newGraph);
		return newGraph;
	}
}
