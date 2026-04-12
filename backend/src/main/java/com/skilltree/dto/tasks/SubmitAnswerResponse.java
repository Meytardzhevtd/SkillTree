package com.skilltree.dto.tasks;

import java.util.List;

public record SubmitAnswerResponse(boolean correct, boolean alreadySolved, String message,
		float moduleProgress, List<TaskSimpleDto> tasks) {
}
