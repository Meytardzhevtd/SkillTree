package com.skilltree.Service;

import org.springframework.stereotype.Service;

import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.model.Task;
import com.skilltree.model.UserTaskScores;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.UserTaskScoresRepository;

@Service
public class UserTaskScoresService {
	/**
	 * Тут сервис для обработки получения баллов
	 */

	private final UserTaskScoresRepository scoresRepository;
	private final TaskRepository taskRepository;

	public UserTaskScoresService(UserTaskScoresRepository scoresRepository,
			TaskRepository taskRepository) {
		this.scoresRepository = scoresRepository;
		this.taskRepository = taskRepository;
	}

	public void add(Long userId, Long taskId) {
		if (scoresRepository.existsByUserIdAndTaskId(userId, taskId) == false) {
			UserTaskScores newUserTaskScores = new UserTaskScores(null, userId, taskId);
			scoresRepository.save(newUserTaskScores);
		}
	}

	public Integer getTotalScore(Long userId) {
		Integer answer = scoresRepository.getTotalScoreByUserId(userId);
		if (answer == null) {
			return 0;
		} else {
			return answer;
		}
	}

	public void changeScore(Long taskId, Integer score) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new TaskNotFoundException(taskId));
		task.setScore(score);
		taskRepository.save(task);
	}
}
