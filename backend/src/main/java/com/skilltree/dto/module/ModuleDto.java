package com.skilltree.dto.module;

import com.skilltree.dto.tasks.TaskSimpleDto;
import com.skilltree.model.Module;
import lombok.Data;

import java.util.List;

@Data
public class ModuleDto {
	private Long id;
	private Long courseId;
	private String name;
	private Boolean can_be_open;
	private List<TaskSimpleDto> tasks;
	private List<Long> blockedBy;

	public ModuleDto(Module module) {
		this.id = module.getId();
		this.courseId = module.getCourse().getId();
		this.name = module.getName();
		this.can_be_open = module.getCan_be_open();

		this.tasks = module.getTasks().stream().map(task -> new TaskSimpleDto(task.getId(), false))
				.toList();

		this.blockedBy = module.getBlockedBy().stream().map(dep -> dep.getMainModule().getId())
				.toList();
	}
}
