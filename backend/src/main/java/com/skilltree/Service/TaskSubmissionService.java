package com.skilltree.Service;

import com.skilltree.dto.content.MultipleAnswerTaskContent;
import com.skilltree.dto.content.OneAnswerTaskContent;
import com.skilltree.dto.content.TaskContent;
import com.skilltree.dto.tasks.SubmitAnswerRequest;
import com.skilltree.dto.tasks.SubmitAnswerResponse;
import com.skilltree.dto.tasks.TaskSimpleDto;
import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.model.*;
import com.skilltree.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TaskSubmissionService {

	private static final Logger log = LoggerFactory.getLogger(TaskSubmissionService.class);

	private final TaskRepository taskRepository;
	private final UserAnswerRepository userAnswerRepository;
	private final ProgressModuleRepository progressModuleRepository;
	private final TakenCoursesRepository takenCoursesRepository;

	public TaskSubmissionService(TaskRepository taskRepository,
			UserAnswerRepository userAnswerRepository,
			ProgressModuleRepository progressModuleRepository,
			TakenCoursesRepository takenCoursesRepository) {
		this.taskRepository = taskRepository;
		this.userAnswerRepository = userAnswerRepository;
		this.progressModuleRepository = progressModuleRepository;
		this.takenCoursesRepository = takenCoursesRepository;
	}

	@Transactional
	public SubmitAnswerResponse submit(Long taskId, SubmitAnswerRequest request) {
		Task task = taskRepository.findById(taskId)
				.orElseThrow(() -> new TaskNotFoundException(taskId));

		ProgressModule progressModule = progressModuleRepository
				.findById(request.progressModuleId()).orElseThrow(() -> new RuntimeException(
						"ProgressModule not found: " + request.progressModuleId()));

		boolean alreadySolved = userAnswerRepository
				.existsCorrectAnswerByTaskAndProgressModule(task, progressModule);

		boolean correct = checkAnswer(task.getContent(), request.answer());

		UserAnswers userAnswer = new UserAnswers();
		userAnswer.setTask(task);
		userAnswer.setProgress_module(progressModule);
		userAnswer.setAnswer(Map.of("value", request.answer()));
		userAnswer.setCorrect(correct);
		userAnswerRepository.save(userAnswer);

		log.info("Попытка сохранена: taskId={}, progressModuleId={}, correct={}", taskId,
				request.progressModuleId(), correct);

		if (correct && !alreadySolved) {
			recalculateModuleProgress(progressModule);
			recalculateCourseProgress(progressModule.getTaken_courses());
		}

		ProgressModule updatedProgressModule = progressModuleRepository
				.findById(progressModule.getId()).orElseThrow();

		List<TaskSimpleDto> taskStatuses = getTaskStatuses(progressModule);

		return new SubmitAnswerResponse(correct, alreadySolved,
				buildMessage(correct, alreadySolved), updatedProgressModule.getProgress(),
				taskStatuses);
	}

	private void recalculateModuleProgress(ProgressModule progressModule) {
		long totalTasks = taskRepository.findByModule(progressModule.getModule()).size();

		if (totalTasks == 0)
			return;

		long solvedTasks = userAnswerRepository
				.countDistinctCorrectTasksByProgressModule(progressModule);

		float newProgress = (float) solvedTasks / totalTasks * 100;
		progressModule.setProgress(newProgress);
		progressModuleRepository.save(progressModule);

		log.info("Прогресс модуля id={} обновлён: {}/{}={}%", progressModule.getId(), solvedTasks,
				totalTasks, newProgress);
	}

	private void recalculateCourseProgress(TakenCourses takenCourses) {
		List<ProgressModule> allProgressModules = takenCourses.getProgress_modules();

		if (allProgressModules.isEmpty())
			return;

		long totalTasksInCourse = 0;
		long solvedTasksInCourse = 0;

		for (ProgressModule pm : allProgressModules) {
			long totalInModule = taskRepository.findByModule(pm.getModule()).size();
			long solvedInModule = userAnswerRepository
					.countDistinctCorrectTasksByProgressModule(pm);
			totalTasksInCourse += totalInModule;
			solvedTasksInCourse += solvedInModule;
		}

		float courseProgress = totalTasksInCourse == 0
				? 0
				: (float) solvedTasksInCourse / totalTasksInCourse * 100;

		takenCourses.setProgress(courseProgress);
		takenCoursesRepository.save(takenCourses);

		log.info("Прогресс курса takenCourseId={} обновлён: {}/{}={}%", takenCourses.getId(),
				solvedTasksInCourse, totalTasksInCourse, courseProgress);
	}

	private List<TaskSimpleDto> getTaskStatuses(ProgressModule progressModule) {
		List<Task> allTasks = taskRepository.findByModule(progressModule.getModule());
		List<UserAnswers> allAnswers = userAnswerRepository.findByProgressModule(progressModule);

		Set<Long> solvedTaskIds = new HashSet<>();
		for (UserAnswers ua : allAnswers) {
			if (ua.isCorrect()) {
				solvedTaskIds.add(ua.getTask().getId());
			}
		}

		return allTasks.stream().sorted((a, b) -> Long.compare(a.getId(), b.getId()))
				.map(t -> new TaskSimpleDto(t.getId(), solvedTaskIds.contains(t.getId()))).toList();
	}

	private boolean checkAnswer(TaskContent content, Object userAnswer) {
		if (content instanceof OneAnswerTaskContent oneAnswer) {
			return checkOneAnswer(oneAnswer, userAnswer);
		} else if (content instanceof MultipleAnswerTaskContent multipleAnswer) {
			return checkMultipleAnswer(multipleAnswer, userAnswer);
		}
		log.warn("Неизвестный тип контента: {}", content.getClass().getSimpleName());
		return false;
	}

	private boolean checkOneAnswer(OneAnswerTaskContent content, Object userAnswer) {
		if (!(userAnswer instanceof Integer selectedIndex)) {
			log.warn("Ожидался Integer, получен: {}", userAnswer.getClass().getSimpleName());
			return false;
		}
		return selectedIndex == content.getIndexCorrectAnswer();
	}

	@SuppressWarnings("unchecked")
	private boolean checkMultipleAnswer(MultipleAnswerTaskContent content, Object userAnswer) {
		if (!(userAnswer instanceof List)) {
			log.warn("Ожидался List, получен: {}", userAnswer.getClass().getSimpleName());
			return false;
		}
		List<Integer> selected = (List<Integer>) userAnswer;
		return new HashSet<>(selected).equals(new HashSet<>(content.getCorrectAnswers()));
	}

	private String buildMessage(boolean correct, boolean alreadySolved) {
		if (alreadySolved) {
			return correct
					? "Верно! Эта задача уже была решена ранее."
					: "Неверно, но эта задача уже была решена ранее.";
		}
		return correct ? "Верно!" : "Неверно, попробуй ещё раз.";
	}
}
