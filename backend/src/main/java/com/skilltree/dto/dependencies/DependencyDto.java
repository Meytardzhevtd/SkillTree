package com.skilltree.dto.dependencies;

import lombok.Data;

@Data
public class DependencyDto {
	private Long id;
	private Long dependentModuleId;
	private String dependentModuleName;
	private boolean isOpen;

	public DependencyDto(Long id, Long dependentModuleId, String dependentModuleName,
			boolean isOpen) {
		this.id = id;
		this.dependentModuleId = dependentModuleId;
		this.dependentModuleName = dependentModuleName;
		this.isOpen = isOpen;
	}
}
