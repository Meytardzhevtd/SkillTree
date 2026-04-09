package com.skilltree.dto;

import com.skilltree.dto.content.TaskContent;
import com.skilltree.model.Task;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
	private Long id;
	private TaskContent content;

	public static TaskDto fromEntity(Task t) {
		TaskDto dto = new TaskDto();
		dto.setId(t.getId());
		dto.setContent(t.getContent());
		return dto;
	}
}