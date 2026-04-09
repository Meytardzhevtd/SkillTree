package com.skilltree.Service;

import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.skilltree.dto.content.MultipleAnswerTaskContent;
import com.skilltree.dto.content.OneAnswerTaskContent;
import com.skilltree.dto.content.TaskContent;
import com.skilltree.dto.tasks.TaskSubmissionResponse;
import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.model.Task;
import com.skilltree.repository.TaskRepository;

@Service
public class TaskSubmissionService {
	private static final Logger log = LoggerFactory.getLogger(TaskSubmissionService.class);

	private final TaskRepository taskRepository;

	public TaskSubmissionService(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	public TaskSubmissionResponse checkAnswer(Long taskId, Object userAnswer) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new TaskNotFoundException(taskId));

		TaskContent content = task.getContent();

		if (content instanceof OneAnswerTaskContent oneAnswer) {
			return checkOneAnswer(oneAnswer, userAnswer);
		} else if (content instanceof MultipleAnswerTaskContent multipleAnswer) {
			return checkMultipleAnswer(multipleAnswer, userAnswer);
		}

		log.warn("Неизвестный тип контента для taskId={}: {}", taskId,
				content.getClass().getSimpleName());
		return new TaskSubmissionResponse(false, "Неизвестный тип задания");
	}

	private TaskSubmissionResponse checkOneAnswer(OneAnswerTaskContent content, Object userAnswer) {
		if (!(userAnswer instanceof Integer selectedIndex)) {
			return new TaskSubmissionResponse(false, "Ожидается индекс ответа (целое число)");
		}

		boolean correct = selectedIndex == content.getIndexCorrectAnswer();
		String message = correct ? "Верно!" : "Неверно, попробуй ещё раз";
		return new TaskSubmissionResponse(correct, message);
	}

	@SuppressWarnings("unchecked")
	private TaskSubmissionResponse checkMultipleAnswer(MultipleAnswerTaskContent content,
			Object userAnswer) {
		if (!(userAnswer instanceof List)) {
			return new TaskSubmissionResponse(false, "Ожидается список индексов ответов");
		}

		List<Integer> selectedIndexes = (List<Integer>) userAnswer;
		List<Integer> correctAnswers = content.getCorrectAnswers();

		boolean correct = new HashSet<>(selectedIndexes).equals(new HashSet<>(correctAnswers));
		String message = correct ? "Верно!" : "Неверно, попробуй ещё раз";
		return new TaskSubmissionResponse(correct, message);
	}
}