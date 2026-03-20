package com.skilltree.dto.module;

import com.skilltree.model.Dependencies;
import com.skilltree.model.Module;
import com.skilltree.model.Task;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModuleDto {
	private Long id;
	private Long courseId;
	private String name;
	private Boolean can_be_open;
	private List<Task> tasks = new ArrayList<>();
	private List<Dependencies> blockedBy = new ArrayList<>();
	public ModuleDto(Module module) {
		this.id = module.getId();
		this.courseId = module.getCourse().getId();
		this.name = module.getName();
		this.can_be_open = module.getCan_be_open();
		this.tasks = module.getTasks();
		this.blockedBy = module.getBlockedBy();
	}
}
