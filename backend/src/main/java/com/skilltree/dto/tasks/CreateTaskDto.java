package com.skilltree.dto.tasks;

import com.skilltree.dto.content.TaskContent;
import com.skilltree.model.Module;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskDto {

	@NotNull(message = "taskTypeId is required")
	private Long taskTypeId;

	@NotNull(message = "moduleId is required")
	private Long moduleId;

	@NotNull(message = "content is required")
	private TaskContent content; // ← ИЗМЕНИЛИ: теперь не Map, а TaskContent

	@SuppressWarnings("unchecked")
	public Task toEntity(TaskTypes type, Module module) {
		Task t = new Task();
		t.setTask_type(type);
		t.setModule(module);

		// Превращаем TaskContent в Map для хранения в БД
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> contentAsMap = mapper.convertValue(this.content, Map.class);
		t.setContent(contentAsMap);

		return t;
	}
}