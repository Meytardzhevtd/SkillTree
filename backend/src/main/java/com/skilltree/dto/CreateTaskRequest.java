package com.skilltree.dto;

import java.util.Map;

public class CreateTaskRequest {
	private Map<String, Object> content;

	public Map<String, Object> getContent() {
		return content;
	}
	public void setContent(Map<String, Object> content) {
		this.content = content;
	}
}