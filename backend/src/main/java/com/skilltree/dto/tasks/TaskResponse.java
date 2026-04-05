package com.skilltree.dto.tasks;

import com.skilltree.dto.content.TaskContent;
import com.skilltree.model.Task;
import lombok.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
	private Long id;
	private Long taskTypeId;
	private Long moduleId;
	private TaskContent content;

	public static TaskResponse of(Task task) {
		if (task == null)
			return null;

		Long typeId = task.getTask_type() != null ? task.getTask_type().getId() : null;
		Long modId = task.getModule() != null ? task.getModule().getId() : null;

		ObjectMapper mapper = new ObjectMapper();
		TaskContent contentDto = null;

		if (task.getContent() != null && typeId != null) {
			String taskTypeName = task.getTask_type().getName();
			Class<? extends TaskContent> targetClass = getContentClassByTypeName(taskTypeName);
			if (targetClass != null) {
				contentDto = mapper.convertValue(task.getContent(), targetClass);
			}
		}

		return new TaskResponse(task.getId(), typeId, modId, contentDto);
	}

	private static Class<? extends TaskContent> getContentClassByTypeName(String typeName) {
		switch (typeName) {
			case "ONE_POSSIBLE_ANSWER" :
				return com.skilltree.dto.content.OneAnswerTaskContent.class;
			case "MULTIPLE" :
				return com.skilltree.dto.content.MultipleAnswerTaskContent.class;
			default :
				return null;
		}
	}
}