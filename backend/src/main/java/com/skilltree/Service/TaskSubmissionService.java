package com.skilltree.Service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.skilltree.dto.tasks.SubmitTaskRequest;
import com.skilltree.dto.tasks.TaskSubmissionResponse;
import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import com.skilltree.repository.TaskRepository;

@Service
public class TaskSubmissionService {
	private final TaskRepository taskRepository;

	public TaskSubmissionService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public TaskSubmissionResponse submitTask(SubmitTaskRequest request) {
		Task task = taskRepository.findById(request.taskId())
				.orElseThrow(() -> new TaskNotFoundException(request.taskId()));

		String taskTypeName = task.getTask_type().getName();

		switch (taskTypeName) {
			case "ONE_POSSIBLE_ANSWER" :

				break;

			default :
				break;
		}

		return null;
	}
}
