package com.skilltree.dto.tasks;

import java.util.Map;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
	@Size(min = 1)
	private Map<String, Object> content;
}
