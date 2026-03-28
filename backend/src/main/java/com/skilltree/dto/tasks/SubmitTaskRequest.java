package com.skilltree.dto.tasks;

import java.util.Map;

public record SubmitTaskRequest(Long taskId, Map<String, Object> answer) {
}
