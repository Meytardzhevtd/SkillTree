package com.skilltree.dto.tasks;

import com.skilltree.dto.content.TaskContent;
import com.skilltree.model.Module;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import jakarta.validation.constraints.NotNull;
import lombok.*;

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
	private TaskContent content;

	@NotNull
	private Integer score = 10;

	public Task toEntity(TaskTypes type, Module module, Integer score) {
		Task t = new Task();
		t.setTask_type(type);
		t.setModule(module);
		t.setContent(this.content);
		t.setScore(score);
		return t;
	}
}