package com.skilltree.dto.dependencies;

import lombok.Data;

@Data
public class DependencyConstructorDto {
	private Long id;
	private Long dependentModuleId;
	private String dependentModuleName;

	public DependencyConstructorDto(Long id, Long dependentModuleId, String dependentModuleName) {
		this.id = id;
		this.dependentModuleId = dependentModuleId;
		this.dependentModuleName = dependentModuleName;
	}
}
