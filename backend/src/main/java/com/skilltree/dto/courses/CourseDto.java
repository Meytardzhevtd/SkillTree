package com.skilltree.dto.courses;

import com.skilltree.dto.module.ModuleDto;
import com.skilltree.model.Courses;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class CourseDto {
	private Long id;
	private String name;
	private String description;
	private List<ModuleDto> modules;
	public CourseDto(Courses course) {
		this.id = course.getId();
		this.name = course.getName();
		this.description = course.getDescription();
		this.modules = course.getModules().stream().map(module -> new ModuleDto(module))
				.collect(Collectors.toList());
	}
}
