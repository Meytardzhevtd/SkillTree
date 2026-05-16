package com.skilltree.dto.dependencies;

import lombok.Data;

@Data
public class DependencyTakeCourseDto {
	private Long id;
	private Long blockedModuleId;
	private String blockedModuleName;
	private boolean isOpen;

	public DependencyTakeCourseDto(Long id, Long blockedModuleId, String blockedModuleName,
			boolean isOpen) {
		this.id = id;
		this.blockedModuleId = blockedModuleId;
		this.blockedModuleName = blockedModuleName;
		this.isOpen = isOpen;
	}
}
