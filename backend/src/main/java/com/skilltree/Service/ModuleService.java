package com.skilltree.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.dto.CreateModuleDto;
import com.skilltree.exception.ModuleNotFoundException;
import com.skilltree.model.Courses;
import com.skilltree.repository.CourseRepository;
import com.skilltree.repository.DependencyRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.ProgressModuleRepository;
import com.skilltree.model.Module;

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

	private Module findModuleOrThrow(Long moduleId) {
		Optional<Module> oModule = moduleRepository.findById(moduleId);
		if (oModule.isPresent()) {
			return oModule.get();
		} else {
			throw new ModuleNotFoundException(moduleId);
		}
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

	/**
	 * Пока не нравится. Мб вообще метод не нужен тк все само по себе должно
	 * вычилсяться через депенденсы
	 */
	// @Transactional
	// public void changeTheModuleAvailable(Long moduleId, Boolean isOpen) {
	// // ставит open_modele = isOpen
	// Optional<Module> oModule = moduleRepository.findById(moduleId);
	// if (oModule.isPresent()) {
	// Module module = oModule.get();
	// module.setCan_be_open(isOpen);
	// moduleRepository.save(module);
	// } else {
	// throw new RuntimeException("Module with id = " + moduleId + " not found");
	// }
	// }

	@Transactional
	public void delete(Long moduleId) {
		if (moduleRepository.existsById(moduleId)) {
			moduleRepository.deleteById(moduleId);
		} else {
			throw new RuntimeException("Module with id = " + moduleId + " not found");
		}
	}

	public Module getModule(Long moduleId) {
		return findModuleOrThrow(moduleId);
	}
}
