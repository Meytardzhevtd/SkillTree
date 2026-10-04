package com.skilltree.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilltree.Service.TaskService;
import com.skilltree.Service.TaskSubmissionService;
import com.skilltree.dto.content.OneAnswerTaskContent;
import com.skilltree.dto.tasks.CreateTaskDto;
import com.skilltree.dto.tasks.SubmitAnswerRequest;
import com.skilltree.dto.tasks.SubmitAnswerResponse;
import com.skilltree.dto.tasks.TaskResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("TaskController")
class TaskControllerTest {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private TaskService taskService;
	@MockBean
	private TaskSubmissionService taskSubmissionService;

	@Test
	@DisplayName("GET /api/tasks/{id} возвращает задание")
	void getTask() throws Exception {
		TaskResponse response = new TaskResponse();
		response.setId(1L);
		response.setTaskTypeId(1L);
		response.setModuleId(10L);
		response.setScore(10);

		when(taskService.get(1L)).thenReturn(response);

		mockMvc.perform(get("/api/tasks/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.score").value(10));
	}

	@Test
	@DisplayName("GET /api/tasks возвращает список по модулю")
	void listByModule() throws Exception {
		TaskResponse t1 = new TaskResponse();
		t1.setId(1L);
		TaskResponse t2 = new TaskResponse();
		t2.setId(2L);

		when(taskService.getAllTasksByModule(eq(10L), eq(null))).thenReturn(List.of(t1, t2));

		mockMvc.perform(get("/api/tasks").param("moduleId", "10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[1].id").value(2));
	}

	@Test
	@DisplayName("GET /api/tasks без moduleId возвращает 400")
	void listWithoutModuleId() throws Exception {
		mockMvc.perform(get("/api/tasks")).andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/tasks создаёт задание и возвращает 201 с Location")
	void createTask() throws Exception {
		CreateTaskDto dto = new CreateTaskDto();
		dto.setTaskTypeId(1L);
		dto.setModuleId(10L);
		dto.setScore(10);

		OneAnswerTaskContent content = new OneAnswerTaskContent();
		content.setQuestion("2+2?");
		content.setOptions(List.of("3", "4", "5"));
		content.setIndexCorrectAnswer(1);
		dto.setContent(content);

		TaskResponse response = new TaskResponse();
		response.setId(100L);

		when(taskService.create(any(CreateTaskDto.class))).thenReturn(response);

		mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))).andExpect(status().isCreated())
				.andExpect(header().string("Location", "http://localhost/api/tasks/100"))
				.andExpect(jsonPath("$.id").value(100));
	}

	@Test
	@DisplayName("POST /api/tasks возвращает 400 при отсутствии обязательных полей")
	void createTaskValidation() throws Exception {
		CreateTaskDto invalid = new CreateTaskDto();

		mockMvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalid)))
				.andExpect(status().isBadRequest());
	}

	@Test
	@DisplayName("POST /api/tasks/{id}/submit возвращает результат проверки")
	void submitAnswer() throws Exception {
		SubmitAnswerRequest request = new SubmitAnswerRequest(10000L, 1);
		SubmitAnswerResponse response = new SubmitAnswerResponse(true, false, "Верно!", 50f,
				List.of());

		when(taskSubmissionService.submit(eq(1L), any(SubmitAnswerRequest.class)))
				.thenReturn(response);

		mockMvc.perform(post("/api/tasks/1/submit").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.correct").value(true))
				.andExpect(jsonPath("$.alreadySolved").value(false))
				.andExpect(jsonPath("$.moduleProgress").value(50.0));
	}

	@Test
	@DisplayName("POST /api/tasks/{id}/submit возвращает 400 при отсутствии progressModuleId")
	void submitAnswerValidation() throws Exception {
		String invalidJson = "{\"answer\": 1}";

		mockMvc.perform(post("/api/tasks/1/submit").contentType(MediaType.APPLICATION_JSON)
				.content(invalidJson)).andExpect(status().isBadRequest());
	}
}