package com.skilltree.Service;

import com.skilltree.dto.content.MultipleAnswerTaskContent;
import com.skilltree.dto.content.OneAnswerTaskContent;
import com.skilltree.dto.tasks.SubmitAnswerRequest;
import com.skilltree.dto.tasks.SubmitAnswerResponse;
import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.model.Courses;
import com.skilltree.model.Module;
import com.skilltree.model.ProgressModule;
import com.skilltree.model.TakenCourses;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import com.skilltree.model.UserAnswers;
import com.skilltree.model.Users;
import com.skilltree.repository.ProgressModuleRepository;
import com.skilltree.repository.TakenCoursesRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.UserAnswerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskSubmissionService")
class TaskSubmissionServiceTest {

	@Mock
	private TaskRepository taskRepository;
	@Mock
	private UserAnswerRepository userAnswerRepository;
	@Mock
	private ProgressModuleRepository progressModuleRepository;
	@Mock
	private TakenCoursesRepository takenCoursesRepository;
	@Mock
	private UserTaskScoresService scoresService;

	@InjectMocks
	private TaskSubmissionService service;

	private Users user;
	private Courses course;
	private Module module;
	private TakenCourses taken;
	private ProgressModule progress;

	@BeforeEach
	void setUp() {
		user = new Users();
		user.setId(1L);
		user.setUsername("u");
		user.setEmail("u@test");
		user.setPassword("p");

		course = new Courses();
		course.setId(10L);
		course.setName("Java");
		course.setDescription("d");

		module = new Module();
		module.setId(100L);
		module.setCourse(course);
		module.setName("Basics");
		module.setCan_be_open(true);

		taken = new TakenCourses();
		taken.setId(1000L);
		taken.setUser(user);
		taken.setCourse(course);
		taken.setProgress(0f);

		progress = new ProgressModule();
		progress.setId(10000L);
		progress.setModule(module);
		progress.setTaken_courses(taken);
		progress.setProgress(0f);
	}

	private Task oneAnswerTask(Long id, int correctIndex, int score) {
		TaskTypes type = new TaskTypes();
		type.setId(1L);
		type.setName("ONE_POSSIBLE_ANSWER");

		OneAnswerTaskContent content = new OneAnswerTaskContent();
		content.setQuestion("2+2?");
		content.setOptions(List.of("3", "4", "5"));
		content.setIndexCorrectAnswer(correctIndex);

		Task t = new Task();
		t.setId(id);
		t.setModule(module);
		t.setTask_type(type);
		t.setContent(content);
		t.setScore(score);
		return t;
	}

	private Task multipleAnswerTask(Long id, List<Integer> correct, int score) {
		TaskTypes type = new TaskTypes();
		type.setId(2L);
		type.setName("MULTIPLE");

		MultipleAnswerTaskContent content = new MultipleAnswerTaskContent();
		content.setQuestion("Выбери чётные");
		content.setOptions(List.of("1", "2", "3", "4"));
		content.setCorrectAnswers(correct);

		Task t = new Task();
		t.setId(id);
		t.setModule(module);
		t.setTask_type(type);
		t.setContent(content);
		t.setScore(score);
		return t;
	}

	@Nested
	@DisplayName("Одиночный ответ")
	class OneAnswer {

		@Test
		@DisplayName("correct=true для верного индекса")
		void correctIndex() {
			Task task = oneAnswerTask(1L, 1, 10);

			when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
			when(progressModuleRepository.findById(10000L)).thenReturn(Optional.of(progress));
			when(userAnswerRepository.existsCorrectAnswerByTaskAndProgressModule(task, progress))
					.thenReturn(false);
			when(taskRepository.sumScoreByModule(module)).thenReturn(10L);
			when(userAnswerRepository.sumScoreOfDistinctCorrectTasks(progress)).thenReturn(10L);
			when(userAnswerRepository.findByProgressModule(progress)).thenReturn(List.of());

			SubmitAnswerResponse resp = service.submit(1L, new SubmitAnswerRequest(10000L, 1));

			assertThat(resp.correct()).isTrue();
			assertThat(resp.alreadySolved()).isFalse();
			verify(userAnswerRepository).save(any(UserAnswers.class));
			verify(scoresService).add(1L, 1L);
		}

		@Test
		@DisplayName("correct=false для неверного индекса")
		void wrongIndex() {
			Task task = oneAnswerTask(1L, 1, 10);

			when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
			when(progressModuleRepository.findById(10000L)).thenReturn(Optional.of(progress));
			when(userAnswerRepository.existsCorrectAnswerByTaskAndProgressModule(task, progress))
					.thenReturn(false);
			when(userAnswerRepository.findByProgressModule(progress)).thenReturn(List.of());

			SubmitAnswerResponse resp = service.submit(1L, new SubmitAnswerRequest(10000L, 0));

			assertThat(resp.correct()).isFalse();
			verify(scoresService, never()).add(any(), any());
		}

		@Test
		@DisplayName("correct=false если пришёл не Integer")
		void wrongType() {
			Task task = oneAnswerTask(1L, 1, 10);

			when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
			when(progressModuleRepository.findById(10000L)).thenReturn(Optional.of(progress));
			when(userAnswerRepository.existsCorrectAnswerByTaskAndProgressModule(task, progress))
					.thenReturn(false);
			when(userAnswerRepository.findByProgressModule(progress)).thenReturn(List.of());

			SubmitAnswerResponse resp = service.submit(1L,
					new SubmitAnswerRequest(10000L, "не число"));

			assertThat(resp.correct()).isFalse();
		}
	}

	@Nested
	@DisplayName("Множественный ответ")
	class MultipleAnswer {

		@Test
		@DisplayName("correct=true когда множества совпадают")
		void exactMatch() {
			Task task = multipleAnswerTask(1L, List.of(1, 3), 10);

			when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
			when(progressModuleRepository.findById(10000L)).thenReturn(Optional.of(progress));
			when(userAnswerRepository.existsCorrectAnswerByTaskAndProgressModule(task, progress))
					.thenReturn(false);
			when(taskRepository.sumScoreByModule(module)).thenReturn(10L);
			when(userAnswerRepository.sumScoreOfDistinctCorrectTasks(progress)).thenReturn(10L);
			when(userAnswerRepository.findByProgressModule(progress)).thenReturn(List.of());

			SubmitAnswerResponse resp = service.submit(1L,
					new SubmitAnswerRequest(10000L, List.of(3, 1)));

			assertThat(resp.correct()).isTrue();
		}

		@Test
		@DisplayName("correct=false когда множества не совпадают")
		void partialMatch() {
			Task task = multipleAnswerTask(1L, List.of(1, 3), 10);

			when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
			when(progressModuleRepository.findById(10000L)).thenReturn(Optional.of(progress));
			when(userAnswerRepository.existsCorrectAnswerByTaskAndProgressModule(task, progress))
					.thenReturn(false);
			when(userAnswerRepository.findByProgressModule(progress)).thenReturn(List.of());

			SubmitAnswerResponse resp = service.submit(1L,
					new SubmitAnswerRequest(10000L, List.of(1)));

			assertThat(resp.correct()).isFalse();
		}
	}

	@Nested
	@DisplayName("Граничные случаи")
	class EdgeCases {

		@Test
		@DisplayName("кидает TaskNotFoundException для несуществующей задачи")
		void taskNotFound() {
			when(taskRepository.findById(999L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.submit(999L, new SubmitAnswerRequest(10000L, 0)))
					.isInstanceOf(TaskNotFoundException.class);
		}

		@Test
		@DisplayName("alreadySolved=true если уже решал верно")
		void alreadySolved() {
			Task task = oneAnswerTask(1L, 1, 10);

			when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
			when(progressModuleRepository.findById(10000L)).thenReturn(Optional.of(progress));
			when(userAnswerRepository.existsCorrectAnswerByTaskAndProgressModule(task, progress))
					.thenReturn(true);
			when(userAnswerRepository.findByProgressModule(progress)).thenReturn(List.of());

			SubmitAnswerResponse resp = service.submit(1L, new SubmitAnswerRequest(10000L, 1));

			assertThat(resp.alreadySolved()).isTrue();
			verify(scoresService, never()).add(any(), any());
		}

		@Test
		@DisplayName("прогресс модуля пересчитывается по score")
		void progressRecalculated() {
			Task task = oneAnswerTask(1L, 1, 20);

			when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
			when(progressModuleRepository.findById(10000L)).thenReturn(Optional.of(progress));
			when(userAnswerRepository.existsCorrectAnswerByTaskAndProgressModule(task, progress))
					.thenReturn(false);
			when(taskRepository.sumScoreByModule(module)).thenReturn(100L);
			when(userAnswerRepository.sumScoreOfDistinctCorrectTasks(progress)).thenReturn(20L);
			when(userAnswerRepository.findByProgressModule(progress)).thenReturn(List.of());

			SubmitAnswerResponse resp = service.submit(1L, new SubmitAnswerRequest(10000L, 1));

			assertThat(resp.moduleProgress()).isEqualTo(20f);
		}
	}
}