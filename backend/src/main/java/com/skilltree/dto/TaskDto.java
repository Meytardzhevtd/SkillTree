package com.skilltree.dto;

import com.skilltree.model.Task;

public class TaskDto {
	private Long id;
	private String content;

	public static TaskDto fromEntity(Task t) {
		TaskDto dto = new TaskDto();
		dto.setId(t.getId());
		dto.setContent(t.getContent());
		return dto;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}