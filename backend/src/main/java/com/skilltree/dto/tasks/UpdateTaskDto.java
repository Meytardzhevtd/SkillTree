package com.skilltree.dto.tasks;

import com.skilltree.dto.content.TaskContent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;

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

	@SuppressWarnings("unchecked")
	public Map<String, Object> getContentAsMap() {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(this.content, Map.class);
	}
}