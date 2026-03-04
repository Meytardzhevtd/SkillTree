package com.skilltree.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.dto.CreateModuleDto;
import com.skilltree.exception.ModuleNotFoundException;
import com.skilltree.model.Courses;
import com.skilltree.model.Dependencies;
import com.skilltree.repository.CourseRepository;
import com.skilltree.repository.DependencyRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.ProgressModuleRepository;
import com.skilltree.model.Module;
import com.skilltree.model.ProgressModule;

@Transactional(readOnly = true)
@Service
public class ModuleService {
	private final ModuleRepository moduleRepository;
	private final CourseRepository courseRepository;
	private final DependencyRepository dependencyRepository;
	private final ProgressModuleRepository progressModuleRepository;

	@Autowired
	public ModuleService(ModuleRepository moduleRepository, CourseRepository courseRepository,
			DependencyRepository dependencyRepository,
			ProgressModuleRepository progressModuleRepository) {
		this.moduleRepository = moduleRepository;
		this.courseRepository = courseRepository;
		this.dependencyRepository = dependencyRepository;
		this.progressModuleRepository = progressModuleRepository;
	}

	@Transactional
	public Module create(CreateModuleDto createModuleDto) {
		Optional<Courses> oCourse = courseRepository.findById(createModuleDto.getCourseId());
		if (oCourse.isPresent() == false) {
			throw new RuntimeException(
					"Course with id = " + createModuleDto.getCourseId() + " not found");
		}
		Module saved = moduleRepository
				.save(new Module(null, oCourse.get(), createModuleDto.getName(), false));
		return saved;
	}

	@Transactional
	public void tryOpenModuleForTakenCourse(Long moduleId, Long takenCourseId) {
		Module module = moduleRepository.findById(moduleId)
				.orElseThrow(() -> new ModuleNotFoundException(moduleId));
		if (Boolean.TRUE.equals(module.getCan_be_open()))
			return;

		// получить все зависимости: какие модули блокируют открытие
		List<Dependencies> deps = dependencyRepository.findByModuleId(moduleId);

		// если зависимостей нет — открываем
		if (deps.isEmpty()) {
			module.setCan_be_open(true);
			moduleRepository.save(module);
			return;
		}

		// проверяем для каждой зависимости наличие прогресса и его порог
		for (Dependencies d : deps) {
			Long blockedModuleId = d.getBlock_module().getId();
			ProgressModule pm = progressModuleRepository
					.findByModuleIdAndTakenCoursesId(blockedModuleId, takenCourseId)
					.orElseThrow(() -> new RuntimeException("Dependency not satisfied: module "
							+ blockedModuleId + " not started for takenCourse " + takenCourseId));

			// критерий завершения — порог; здесь считаем 1.0 (100%) завершением
			if (pm.getProgress() < 1.0f) {
				throw new RuntimeException(
						"Dependency not satisfied: module " + blockedModuleId + " progress < 100%");
			}
		}

		module.setCan_be_open(true);
		moduleRepository.save(module);
	}

	public Module getModule(Long moduleId) {
		return moduleRepository.findById(moduleId)
				.orElseThrow(() -> new ModuleNotFoundException(moduleId));
	}
}
