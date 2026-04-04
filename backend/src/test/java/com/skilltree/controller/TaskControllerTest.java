package com.skilltree.controller;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.skilltree.Service.TaskService;
import com.skilltree.dto.tasks.CreateTaskDto;
import com.skilltree.dto.tasks.TaskResponse;
import com.skilltree.dto.tasks.UpdateTaskDto;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private TaskService taskService;

	@MockitoBean
	private com.skilltree.Service.JwtService jwtService;

	@Test
	void create_success_returnsCreatedAndLocation_and_passesDtoToService() throws Exception {
		String requestJson = "{\"taskTypeId\":1,\"moduleId\":2,\"content\":{\"q\":\"what\"}}";

		Map<String, Object> content = Map.of("q", "what");
		TaskResponse resp = new TaskResponse(10L, 1L, 2L, content);
		when(taskService.create(any(CreateTaskDto.class))).thenReturn(resp);

		ArgumentCaptor<CreateTaskDto> captor = ArgumentCaptor.forClass(CreateTaskDto.class);

		mvc.perform(post("/api/tasks").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isCreated())
				.andExpect(header().string("Location", containsString("/api/tasks/10")))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.id").value(10)).andExpect(jsonPath("$.taskTypeId").value(1))
				.andExpect(jsonPath("$.moduleId").value(2))
				.andExpect(jsonPath("$.content.q").value("what"));

		verify(taskService).create(captor.capture());
		CreateTaskDto passed = captor.getValue();
		assert passed.getTaskTypeId().equals(1L);
		assert passed.getModuleId().equals(2L);
		assert passed.getContent().equals(content);
	}

	@Test
	void get_existing_returnsOkWithBody() throws Exception {
		Map<String, Object> content = Map.of("a", 1);
		TaskResponse resp = new TaskResponse(5L, 1L, 2L, content);
		when(taskService.get(5L)).thenReturn(resp);

		mvc.perform(get("/api/tasks/5")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(5)).andExpect(jsonPath("$.content.a").value(1));
	}

	@Test
	void update_setsIdFromPath_andReturnsOk() throws Exception {
		String requestJson = "{\"taskTypeId\":3,\"content\":{\"x\":\"y\"}}";

		Map<String, Object> content = Map.of("x", "y");
		TaskResponse resp = new TaskResponse(7L, 3L, 2L, content);
		when(taskService.update(any(UpdateTaskDto.class))).thenReturn(resp);

		ArgumentCaptor<UpdateTaskDto> captor = ArgumentCaptor.forClass(UpdateTaskDto.class);

		mvc.perform(
				put("/api/tasks/7").contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value(7))
				.andExpect(jsonPath("$.taskTypeId").value(3));

		verify(taskService).update(captor.capture());
		UpdateTaskDto passed = captor.getValue();
		assert passed.getId().equals(7L);
		assert passed.getTaskTypeId().equals(3L);
		assert passed.getContent().equals(content);
	}

	@Test
	void delete_callsServiceAndReturnsNoContent() throws Exception {
		doNothing().when(taskService).delete(20L);

		mvc.perform(delete("/api/tasks/20")).andExpect(status().isNoContent());

		verify(taskService).delete(20L);
	}

	@Test
	void listByModule_returnsList() throws Exception {
		TaskResponse r1 = new TaskResponse(1L, 1L, 5L, Map.of("i", 1));
		TaskResponse r2 = new TaskResponse(2L, 1L, 5L, Map.of("i", 2));
		when(taskService.getAllTasksByModule(5L)).thenReturn(List.of(r1, r2));

		mvc.perform(get("/api/tasks").param("moduleId", "5")).andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].id").value(1));
	}

	@Test
	void listByModule_missingParam_returnsBadRequest() throws Exception {
		mvc.perform(get("/api/tasks")).andExpect(status().isBadRequest());
		verify(taskService, never()).getAllTasksByModule(any());
	}
}
