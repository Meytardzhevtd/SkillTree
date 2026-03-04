package com.skilltree.dto.tasks;

import com.skilltree.model.Module;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.Map;

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
	@Size(min = 1, message = "content must not be empty")
	private Map<String, Object> content;

	/**
	 * Построить сущность `Task` на основе загруженных сущностей. DTO не обращается
	 * к репозиториям; перед вызовом этого метода сервис должен загрузить
	 * `TaskTypes` и `Module` по id.
	 *
	 * @param type
	 *            загруженный TaskTypes
	 * @param module
	 *            загруженный Module
	 * @return новая сущность Task
	 */
	public Task toEntity(TaskTypes type, Module module) {
		Task t = new Task();
		t.setTask_type(type);
		t.setModule(module);
		t.setContent(this.content);
		return t;
	}
}
