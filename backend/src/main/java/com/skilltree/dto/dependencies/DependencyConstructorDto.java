package com.skilltree.dto.dependencies;

import lombok.Data;

@Data
public class DependencyConstructorDto {
	private Long id;
	private Long blockedModuleId;
	private String blockedModuleName;

	public DependencyConstructorDto(Long id, Long blockedModuleId, String blockedModuleName) {
		this.id = id;
		this.blockedModuleId = blockedModuleId;
		this.blockedModuleName = blockedModuleName;
	}
}
