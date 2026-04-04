package com.skilltree.dto.tasks;

import lombok.*;

import java.util.Map;

import com.skilltree.model.Task;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
	private Long id;
	private Long taskTypeId;
	private Long moduleId;
	private Map<String, Object> content;

	public static TaskResponse of(Task t) {
		if (t == null)
			return null;
		Long typeId = t.getTask_type() != null ? t.getTask_type().getId() : null;
		Long modId = t.getModule() != null ? t.getModule().getId() : null;
		return new TaskResponse(t.getId(), typeId, modId, t.getContent());
	}
}
