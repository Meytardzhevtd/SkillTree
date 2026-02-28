package com.skilltree.dto;

import com.skilltree.model.Module;

import java.util.List;
import java.util.stream.Collectors;

public class ModuleDto {
	private Long id;
	private String name;
	private List<TaskDto> tasks;

	public static ModuleDto fromEntity(Module m) {
		ModuleDto dto = new ModuleDto();
		dto.setId(m.getId());
		dto.setName(m.getName());
		dto.setTasks(m.getTasks().stream().map(TaskDto::fromEntity).collect(Collectors.toList()));
		return dto;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TaskDto> getTasks() {
		return tasks;
	}

	public void setTasks(List<TaskDto> tasks) {
		this.tasks = tasks;
	}
}