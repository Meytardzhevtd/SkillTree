package com.skilltree.Service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import com.skilltree.dto.tasks.CreateTaskDto;
import com.skilltree.dto.tasks.TaskResponse;
import com.skilltree.dto.tasks.UpdateTaskDto;
import com.skilltree.exception.ModuleNotFoundException;
import com.skilltree.exception.TaskNotFoundException;
import com.skilltree.exception.TaskTypesNotFound;
import com.skilltree.model.Module;
import com.skilltree.model.Task;
import com.skilltree.model.TaskTypes;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.TaskTypeRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private TaskTypeRepository taskTypeRepository;

	@Mock
	private ModuleRepository moduleRepository;

	@InjectMocks
	private TaskService taskService;

	private TaskTypes type1;
	private Module module1;

	@BeforeEach
	void setUp() {
		type1 = new TaskTypes(1L, "TYPE1", new ArrayList<>());
		module1 = new Module(2L, null, "mod", true);
	}

	@Test
	void create_whenTypeAndModuleExist_shouldSaveAndReturnResponse() {
		Map<String, Object> content = Map.of("k", "v");
		CreateTaskDto dto = new CreateTaskDto(1L, 2L, content);

		when(taskTypeRepository.findById(1L)).thenReturn(Optional.of(type1));
		when(moduleRepository.findById(2L)).thenReturn(Optional.of(module1));

		Task saved = new Task();
		saved.setId(10L);
		saved.setTask_type(type1);
		saved.setModule(module1);
		saved.setContent(content);

		when(taskRepository.save(any(Task.class))).thenReturn(saved);

		ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
		TaskResponse res = taskService.create(dto);

		assertNotNull(res);
		assertEquals(10L, res.getId());
		assertEquals(1L, res.getTaskTypeId());
		assertEquals(2L, res.getModuleId());
		assertEquals(content, res.getContent());

		verify(taskRepository).save(captor.capture());

		Task passed = captor.getValue();
		assertNotNull(passed.getTask_type());
		assertEquals(1L, passed.getTask_type().getId());
		assertNotNull(passed.getModule());
		assertEquals(2L, passed.getModule().getId());
		assertEquals(content, passed.getContent());
	}

	@Test
	void create_shouldCaptureSavedEntity_fieldsSetCorrectly() {
		Map<String, Object> content = new HashMap<>();
		content.put("x", 123);
		CreateTaskDto dto = new CreateTaskDto(1L, 2L, content);

		when(taskTypeRepository.findById(1L)).thenReturn(Optional.of(type1));
		when(moduleRepository.findById(2L)).thenReturn(Optional.of(module1));

		when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
			Task t = inv.getArgument(0);
			t.setId(77L);
			return t;
		});

		ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
		TaskResponse res = taskService.create(dto);

		assertEquals(77L, res.getId());

		verify(taskRepository).save(captor.capture());
		Task passed = captor.getValue();
		assertSame(type1, passed.getTask_type());
		assertSame(module1, passed.getModule());
		assertEquals(content, passed.getContent());
	}

	@Test
	void create_whenTypeMissing_shouldThrow() {
		CreateTaskDto dto = new CreateTaskDto(1L, 2L, Map.of("k", "v"));

		when(taskTypeRepository.findById(1L)).thenReturn(Optional.empty());

		assertThrows(TaskTypesNotFound.class, () -> taskService.create(dto));
		verify(taskRepository, never()).save(any());
	}

	@Test
	void create_whenModuleMissing_shouldThrow() {
		CreateTaskDto dto = new CreateTaskDto(1L, 2L, Map.of("k", "v"));

		when(taskTypeRepository.findById(1L)).thenReturn(Optional.of(type1));
		when(moduleRepository.findById(2L)).thenReturn(Optional.empty());

		assertThrows(ModuleNotFoundException.class, () -> taskService.create(dto));
		verify(taskRepository, never()).save(any());
	}

	@Test
	void get_whenExists_shouldReturn() {
		Task t = new Task();
		t.setId(5L);
		t.setTask_type(type1);
		t.setModule(module1);
		t.setContent(Map.of("a", 1));

		when(taskRepository.findById(5L)).thenReturn(Optional.of(t));

		TaskResponse res = taskService.get(5L);

		assertEquals(5L, res.getId());
		assertEquals(1L, res.getTaskTypeId());
		assertEquals(2L, res.getModuleId());
		assertEquals(Map.of("a", 1), res.getContent());
	}

	@Test
	void taskResponse_of_null_returnsNull() {
		assertNull(TaskResponse.of(null));
	}

	@Test
	void get_whenMissing_shouldThrow() {
		when(taskRepository.findById(99L)).thenReturn(Optional.empty());
		assertThrows(TaskNotFoundException.class, () -> taskService.get(99L));
	}

	@Test
	void update_whenAllExist_shouldUpdateAndReturn() {
		Task existing = new Task();
		existing.setId(7L);
		existing.setTask_type(type1);
		existing.setModule(module1);
		existing.setContent(Map.of("old", true));

		TaskTypes newType = new TaskTypes(3L, "T3", new ArrayList<>());

		UpdateTaskDto dto = new UpdateTaskDto(7L, 3L, Map.of("new", "ok"));

		when(taskRepository.findById(7L)).thenReturn(Optional.of(existing));
		when(taskTypeRepository.findById(3L)).thenReturn(Optional.of(newType));
		when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

		ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
		TaskResponse res = taskService.update(dto);

		assertEquals(7L, res.getId());
		assertEquals(3L, res.getTaskTypeId());
		assertEquals(2L, res.getModuleId());
		assertEquals(Map.of("new", "ok"), res.getContent());

		verify(taskRepository).save(captor.capture());

		Task passed = captor.getValue();
		assertSame(existing, passed);
		assertEquals(Map.of("new", "ok"), existing.getContent());
		assertSame(newType, existing.getTask_type());
	}

	@Test
	void update_whenTaskMissing_shouldThrow() {
		UpdateTaskDto dto = new UpdateTaskDto(8L, 1L, Map.of("x", 1));
		when(taskRepository.findById(8L)).thenReturn(Optional.empty());
		assertThrows(TaskNotFoundException.class, () -> taskService.update(dto));
	}

	@Test
	void update_whenTypeMissing_shouldThrow() {
		Task existing = new Task();
		existing.setId(11L);
		existing.setTask_type(type1);
		existing.setModule(module1);
		existing.setContent(Map.of("a", 1));

		UpdateTaskDto dto = new UpdateTaskDto(11L, 99L, Map.of("b", 2));

		when(taskRepository.findById(11L)).thenReturn(Optional.of(existing));
		when(taskTypeRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(TaskTypesNotFound.class, () -> taskService.update(dto));
		verify(taskRepository, never()).save(any());
	}

	@Test
	void delete_whenExists_shouldDelete() {
		Task t = new Task();
		t.setId(20L);

		when(taskRepository.findById(20L)).thenReturn(Optional.of(t));

		taskService.delete(20L);

		verify(taskRepository).delete(t);
	}

	@Test
	void delete_whenMissing_shouldThrow() {
		when(taskRepository.findById(100L)).thenReturn(Optional.empty());
		assertThrows(TaskNotFoundException.class, () -> taskService.delete(100L));
	}

	@Test
	void getAllTasksByModule_whenModuleExists_shouldReturnSortedResponses() {
		Module m = new Module(50L, null, "m50", true);

		Task t1 = new Task();
		t1.setId(3L);
		t1.setTask_type(type1);
		t1.setModule(m);
		t1.setContent(Map.of("i", 1));

		Task t2 = new Task();
		t2.setId(1L);
		t2.setTask_type(type1);
		t2.setModule(m);
		t2.setContent(Map.of("i", 2));

		when(moduleRepository.findById(50L)).thenReturn(Optional.of(m));
		when(taskRepository.findByModule(m)).thenReturn(List.of(t1, t2));

		List<TaskResponse> list = taskService.getAllTasksByModule(50L);

		assertEquals(2, list.size());
		assertEquals(1L, list.get(0).getId());
		assertEquals(3L, list.get(1).getId());
	}

	@Test
	void getAllTasksByModule_whenNoTasks_shouldReturnEmptyList() {
		Module m = new Module(60L, null, "m60", true);
		when(moduleRepository.findById(60L)).thenReturn(Optional.of(m));
		when(taskRepository.findByModule(m)).thenReturn(Collections.emptyList());

		List<TaskResponse> list = taskService.getAllTasksByModule(60L);
		assertNotNull(list);
		assertTrue(list.isEmpty());
	}

	@Test
	void getAllTasksByModule_whenModuleMissing_shouldThrow() {
		when(moduleRepository.findById(999L)).thenReturn(Optional.empty());
		assertThrows(ModuleNotFoundException.class, () -> taskService.getAllTasksByModule(999L));
	}
}
