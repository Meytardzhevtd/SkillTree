package com.skilltree.Service;

import com.skilltree.dto.dependencies.DependencyDto;
import com.skilltree.model.Dependencies;
import com.skilltree.model.Module;
import com.skilltree.model.ProgressModule;
import com.skilltree.repository.DependencyRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.ProgressModuleRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DependenciesService {
	private final ModuleRepository moduleRepository;
	private final DependencyRepository dependencyRepository;
	private final ProgressModuleRepository progressModuleRepository;

	public DependenciesService(ModuleRepository moduleRepository,
			DependencyRepository dependencyRepository,
			ProgressModuleRepository progressModuleRepository) {
		this.moduleRepository = moduleRepository;
		this.dependencyRepository = dependencyRepository;
		this.progressModuleRepository = progressModuleRepository;
	}

	private void dfs(Long node, HashMap<Long, List<Long>> graph) {
		// граф должен быть деревом, поэтому без проверки на то, что
		// снова зайдем в одну и ту же вершину во время обхода
		List<Long> list = dependencyRepository.findByModuleId(node).stream()
				.map(dependencies -> dependencies.getBlock_module().getId())
				.collect(Collectors.toList());
		graph.put(node, list);
		for (Long next : list) {
			dfs(next, graph);
		}
	}

	private HashMap<Long, List<Long>> makeGraph(Long root) {
		HashMap<Long, List<Long>> graph = new HashMap<>();
		dfs(root, graph);
		return graph;
	}

	private boolean checkForCycles(Long node, Long neverReach, HashMap<Long, List<Long>> graph) {
		boolean returnValue = false;
		if (graph.get(node) != null) {
			for (Long next : graph.get(node)) {
				if (next.equals(neverReach)) {
					return true;
				}
				returnValue = checkForCycles(next, neverReach, graph);
				if (returnValue) {
					break;
				}
			}
		}
		return returnValue;
	}

	private boolean checkForModule(Long node, Long search, HashMap<Long, List<Long>> graph) {
		if (graph.get(node) != null) {
			for (Long next : graph.get(node)) {
				if (next.equals(search)) {
					return true;
				}
			}
		}
		return false;
	}

	@Transactional
	public boolean makeDependent(Long idModuleMain, Long idModuleDependent) {
		if (idModuleMain.equals(idModuleDependent)) {
			// TODO: передавать соответствующее сообщение об ошибке
			return false;
		}

		HashMap<Long, List<Long>> graph = makeGraph(idModuleMain);
		if (!checkForCycles(idModuleDependent, idModuleMain, graph)) {
			if (graph.containsKey(idModuleMain)) {
				if (!checkForModule(idModuleMain, idModuleDependent, graph)) {
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
		dep.setModule(moduleRepository.findById(idModuleMain).orElseThrow(
				() -> new RuntimeException("Модуль с id " + idModuleMain + " не найден")));
		dep.setBlock_module(moduleRepository.findById(idModuleDependent).orElseThrow(
				() -> new RuntimeException("Модуль с id " + idModuleDependent + " не найден")));
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

	private void dfs2(Long takenCourseId, Long node, HashMap<Long, List<DependencyDto>> newGraph,
			HashMap<Long, List<Long>> graph) {
		if (graph.get(node) != null) {
			for (Long next : graph.get(node)) {
				dfs2(takenCourseId, next, newGraph, graph);
			}
			List<DependencyDto> list = graph.get(node).stream().map(moduleId -> {
				Module module = moduleRepository.findById(moduleId).orElseThrow(
						() -> new RuntimeException("Модуль с id " + moduleId + " не найден"));
				Dependencies dependencies = dependencyRepository
						.findByModuleIdAndBlockModuleId(node, moduleId);
				return new DependencyDto(dependencies.getId(), moduleId, module.getName(),
						checkIsOpen(takenCourseId, moduleId));
			}).toList();
			newGraph.put(node, list);
		}
	}

	public HashMap<Long, List<DependencyDto>> getGraphOfModules(Long takenCourseId, Long root) {
		HashMap<Long, List<Long>> graph = makeGraph(root);
		HashMap<Long, List<DependencyDto>> newGraph = new HashMap<>();
		dfs2(takenCourseId, root, newGraph, graph);
		return newGraph;
	}

	public void deleteDependence(Long idDependence) {
		Dependencies dependence = dependencyRepository.findById(idDependence)
				.orElseThrow(() -> new RuntimeException("Такой зависимости нет"));
		dependencyRepository.delete(dependence);
	}

}
