package com.skilltree.dto.dependencies;

import lombok.Data;

@Data
public class DependencyTakeCourseDto {
	private Long id;
	private Long dependentModuleId;
	private String dependentModuleName;
	private boolean isOpen;

	public DependencyTakeCourseDto(Long id, Long dependentModuleId, String dependentModuleName,
			boolean isOpen) {
		this.id = id;
		this.dependentModuleId = dependentModuleId;
		this.dependentModuleName = dependentModuleName;
		this.isOpen = isOpen;
	}
}
