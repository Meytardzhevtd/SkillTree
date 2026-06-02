package com.skilltree.Service;

import com.skilltree.dto.dependencies.DependencyConstructorDto;
import com.skilltree.dto.dependencies.DependencyTakeCourseDto;
import com.skilltree.model.Dependencies;
import com.skilltree.model.Module;
import com.skilltree.model.ProgressModule;
import com.skilltree.model.TakenCourses;
import com.skilltree.repository.DependencyRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.ProgressModuleRepository;
import com.skilltree.repository.TakenCoursesRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DependenciesService {
	private final ModuleRepository moduleRepository;
	private final DependencyRepository dependencyRepository;
	private final ProgressModuleRepository progressModuleRepository;
	private final TakenCoursesRepository takenCoursesRepository;

	public DependenciesService(ModuleRepository moduleRepository,
			DependencyRepository dependencyRepository,
			ProgressModuleRepository progressModuleRepository,
			TakenCoursesRepository takenCoursesRepository) {
		this.moduleRepository = moduleRepository;
		this.dependencyRepository = dependencyRepository;
		this.progressModuleRepository = progressModuleRepository;
		this.takenCoursesRepository = takenCoursesRepository;
	}

	private void dfs(Long node, HashMap<Long, List<Long>> graph) {
		// граф должен быть деревом, поэтому без проверки на то, что
		// снова зайдем в одну и ту же вершину во время обхода
		List<Long> list = dependencyRepository.findByMainModuleId(node).stream()
				.map(dependencies -> dependencies.getBlockedModule().getId())
				.collect(Collectors.toList());
		graph.put(node, list);
		for (Long next : list) {
			dfs(next, graph);
		}
	}

	private HashMap<Long, List<Long>> makeTree(Long root) {
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
	public boolean makeDependent(Long idMainModule, Long idBlockedModule) {
		if (idMainModule.equals(idBlockedModule)) {
			// TODO: передавать соответствующее сообщение об ошибке
			return false;
		}

		HashMap<Long, List<Long>> graph = makeTree(idBlockedModule);
		if (!checkForCycles(idBlockedModule, idMainModule, graph)) {
			if (graph.containsKey(idMainModule)) {
				if (!checkForModule(idMainModule, idBlockedModule, graph)) {
					graph.get(idMainModule).add(idBlockedModule);
				} else {
					// TODO: передавать соответствующее сообщение об ошибке, о том что уже зависит
					// от модуля
					return false;
				}
			} else {
				graph.put(idMainModule, new ArrayList<>(List.of(idBlockedModule)));
			}
		} else {
			// TODO: передавать соответствующее сообщение об ошибке
			return false;
		}
		Dependencies dep = new Dependencies();
		dep.setMainModule(moduleRepository.findById(idMainModule).orElseThrow(
				() -> new RuntimeException("Модуль с id " + idMainModule + " не найден")));
		dep.setBlockedModule(moduleRepository.findById(idBlockedModule).orElseThrow(
				() -> new RuntimeException("Модуль с id " + idBlockedModule + " не найден")));
		dependencyRepository.save(dep);
		return true;
	}

	public HashMap<Long, List<DependencyTakeCourseDto>> getUserCourseGraph(Long takenCourseId) {
		TakenCourses taken = takenCoursesRepository.findById(takenCourseId)
				.orElseThrow(() -> new RuntimeException("не существует такого выбранного курса"));
		Long courseId = taken.getCourse().getId();

		List<Module> modules = moduleRepository.findByCourseIdOrderById(courseId);
		List<Dependencies> allDeps = dependencyRepository.findAllByCourseId(courseId);
		List<ProgressModule> allProgress = progressModuleRepository
				.findAllByTakenCourseId(takenCourseId);

		Map<Long, List<Dependencies>> depsByMain = new HashMap<>();
		for (Dependencies dep : allDeps) {
			Long mainId = dep.getMainModule().getId();
			depsByMain.computeIfAbsent(mainId, k -> new ArrayList<>()).add(dep);
		}

		Map<Long, List<Long>> blockersByModule = new HashMap<>();
		for (Dependencies dep : allDeps) {
			Long blockedId = dep.getBlockedModule().getId();
			blockersByModule.computeIfAbsent(blockedId, k -> new ArrayList<>())
					.add(dep.getMainModule().getId());
		}

		Map<Long, Float> progressByModule = new HashMap<>();
		for (ProgressModule pm : allProgress) {
			progressByModule.put(pm.getModule().getId(), pm.getProgress());
		}

		Map<Long, Boolean> isOpen = new HashMap<>();
		for (Module module : modules) {
			Long moduleId = module.getId();
			List<Long> blockers = blockersByModule.getOrDefault(moduleId, List.of());
			boolean flag = true;
			for (Long blockerId : blockers) {
				if (progressByModule.get(blockerId) < 97f) {
					flag = false;
					break;
				}
			}
			isOpen.put(moduleId, flag);
		}

		HashMap<Long, List<DependencyTakeCourseDto>> graph = new HashMap<>();
		for (Module module : modules) {
			Long id = module.getId();
			List<Dependencies> deps = depsByMain.getOrDefault(id, List.of());
			List<DependencyTakeCourseDto> dtos = new ArrayList<>();
			for (Dependencies dep : deps) {
				Long blockedId = dep.getBlockedModule().getId();
				dtos.add(new DependencyTakeCourseDto(dep.getId(), blockedId,
						dep.getBlockedModule().getName(), isOpen.get(blockedId)));
			}
			graph.put(id, dtos);
		}
		return graph;
	}

	public void deleteDependence(Long idDependence) {
		Dependencies dependence = dependencyRepository.findById(idDependence)
				.orElseThrow(() -> new RuntimeException("Такой зависимости нет"));
		dependencyRepository.delete(dependence);
	}

	public HashMap<Long, List<DependencyConstructorDto>> getCourseGraph(Long courseId) {
		List<Long> moduleIds = moduleRepository.findByCourseIdOrderById(courseId).stream()
				.map(module -> module.getId()).toList();
		HashMap<Long, List<DependencyConstructorDto>> graph = new HashMap<>();
		for (Long id : moduleIds) {
			List<DependencyConstructorDto> deps = dependencyRepository.findByMainModuleId(id)
					.stream()
					.map(dependencies -> new DependencyConstructorDto(dependencies.getId(),
							dependencies.getBlockedModule().getId(),
							dependencies.getBlockedModule().getName()))
					.toList();
			graph.put(id, deps);
		}
		return graph;
	}

}
