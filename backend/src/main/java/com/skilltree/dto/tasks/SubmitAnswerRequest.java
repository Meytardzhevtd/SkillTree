package com.skilltree.dto.tasks;

import jakarta.validation.constraints.NotNull;

public record SubmitAnswerRequest(
		@NotNull(message = "progressModuleId is required") Long progressModuleId,

		@NotNull(message = "answer is required") Object answer) {
}
