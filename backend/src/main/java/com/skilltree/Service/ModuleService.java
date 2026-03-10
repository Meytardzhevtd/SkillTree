package com.skilltree.Service;

import java.util.List;

import com.skilltree.dto.module.ModuleDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.dto.module.CreateModuleRequest;
import com.skilltree.exception.CourseNotFoundException;
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
	public ModuleDto createModule(CreateModuleRequest request){
		Module module = new Module();
		module.setId(request.getCourseId());
		module.setName(request.getName());
		module.setCan_be_open(request.getCan_be_open());
		return new ModuleDto(moduleRepository.save(module));
	}

}
