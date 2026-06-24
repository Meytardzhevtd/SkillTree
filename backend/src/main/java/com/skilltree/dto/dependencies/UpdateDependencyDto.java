package com.skilltree.dto.dependencies;

import lombok.Data;

@Data
public class UpdateDependencyDto {
	Long mainModuleId;
	Long blockedModuleId;
}
