package com.skilltree.dto.tasks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.skilltree.dto.content.TaskContent;
import com.skilltree.model.Task;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
	private Long id;
	private Long taskTypeId;
	private Long moduleId;
	private TaskContent content;

	@JsonProperty("isCompleted")
	private boolean isCompleted;

	public static TaskResponse of(Task task) {
		if (task == null)
			return null;
		Long typeId = task.getTask_type() != null ? task.getTask_type().getId() : null;
		Long modId = task.getModule() != null ? task.getModule().getId() : null;
		return new TaskResponse(task.getId(), typeId, modId, task.getContent(), false);
	}

	public static TaskResponse of(Task task, boolean isCompleted) {
		if (task == null)
			return null;
		Long typeId = task.getTask_type() != null ? task.getTask_type().getId() : null;
		Long modId = task.getModule() != null ? task.getModule().getId() : null;
		return new TaskResponse(task.getId(), typeId, modId, task.getContent(), isCompleted);
	}
}