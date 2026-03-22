package com.skilltree.dto;

import com.skilltree.model.Task;
import lombok.*;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
	private Long id;
	private Map<String, Object> content;

	public static TaskDto fromEntity(Task t) {
		TaskDto dto = new TaskDto();
		dto.setId(t.getId());
		dto.setContent(t.getContent());
		return dto;
	}
}