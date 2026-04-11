package com.skilltree.dto.takeCourse;

import com.skilltree.model.ProgressModule;

public record ModuleProgressInfo(Long moduleId, String moduleName, float progress) {
	public ModuleProgressInfo(ProgressModule pm) {
		this(pm.getModule().getId(), pm.getModule().getName(), pm.getProgress());
	}
}