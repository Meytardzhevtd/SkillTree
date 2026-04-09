package com.skilltree.dto.tasks;

import com.skilltree.dto.content.TaskContent;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskDto {
	@NotNull
	private Long id;

	@NotNull
	private Long taskTypeId;

	@NotNull
	private TaskContent content;
}