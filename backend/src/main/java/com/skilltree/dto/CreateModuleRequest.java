package com.skilltree.dto;

import java.util.List;

public class CreateModuleRequest {
	private String name;
	private List<CreateTaskRequest> tasks;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public List<CreateTaskRequest> getTasks() {
		return tasks;
	}
	public void setTasks(List<CreateTaskRequest> tasks) {
		this.tasks = tasks;
	}
}