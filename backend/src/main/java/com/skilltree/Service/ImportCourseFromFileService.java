package com.skilltree.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilltree.dto.courses.CreateCourseRequest;
import com.skilltree.dto.lessons.CreateLessonRequest;
import com.skilltree.dto.module.CreateModuleRequest;
import com.skilltree.dto.tasks.CreateTaskDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ImportCourseFromFileService {

	private final CourseService courseService;
	private final ModuleService moduleService;
	private final LessonService lessonService;
	private final TaskService taskService;
	private final ObjectMapper objectMapper;

	public ImportCourseFromFileService(CourseService courseService, ModuleService moduleService,
			LessonService lessonService, TaskService taskService, ObjectMapper objectMapper) {
		this.courseService = courseService;
		this.moduleService = moduleService;
		this.lessonService = lessonService;
		this.taskService = taskService;
		this.objectMapper = objectMapper;
	}

	@SuppressWarnings("unchecked")
	@Transactional
	public Long importCourseFromJson(MultipartFile file) throws IOException {
		Map<String, Object> root = objectMapper.readValue(file.getInputStream(), Map.class);

		// 1. Создаём курс
		CreateCourseRequest courseReq = new CreateCourseRequest();
		courseReq.setName((String) root.get("name"));
		courseReq.setDescription((String) root.get("description"));
		var course = courseService.createCourse(courseReq);

		// 2. Создаём модули
		List<Map<String, Object>> modules = (List<Map<String, Object>>) root.get("modules");
		for (Map<String, Object> moduleMap : modules) {
			CreateModuleRequest moduleReq = new CreateModuleRequest();
			moduleReq.setCourseId(course.getId());
			moduleReq.setName((String) moduleMap.get("name"));
			moduleReq.setCan_be_open((Boolean) moduleMap.getOrDefault("canBeOpen", false));
			var module = moduleService.createModule(moduleReq);

			// 3. Создаём уроки
			List<Map<String, String>> lessons = (List<Map<String, String>>) moduleMap
					.get("lessons");
			if (lessons != null) {
				for (Map<String, String> lessonMap : lessons) {
					CreateLessonRequest lessonReq = new CreateLessonRequest();
					lessonReq.setModuleId(module.moduleId());
					lessonReq.setTitle(lessonMap.get("title"));
					lessonReq.setContent(lessonMap.get("content"));
					lessonService.createLesson(lessonReq);
				}
			}

			// 4. Создаём задачи
			List<Map<String, Object>> tasks = (List<Map<String, Object>>) moduleMap.get("tasks");
			if (tasks != null) {
				for (Map<String, Object> taskMap : tasks) {
					CreateTaskDto taskReq = new CreateTaskDto();
					taskReq.setModuleId(module.moduleId());
					taskReq.setTaskTypeId(Long.valueOf((Integer) taskMap.get("taskTypeId")));
					taskReq.setScore((Integer) taskMap.getOrDefault("score", 10));

					// Формируем content в зависимости от типа
					if (taskReq.getTaskTypeId() == 1) {
						Map<String, Object> content = Map.of("type", "ONE_POSSIBLE_ANSWER",
								"question", taskMap.get("question"), "options",
								taskMap.get("options"), "indexCorrectAnswer",
								taskMap.get("correctIndex"));
						taskReq.setContent(objectMapper.convertValue(content,
								com.skilltree.dto.content.TaskContent.class));
					} else {
						Map<String, Object> content = Map.of("type", "MULTIPLE", "question",
								taskMap.get("question"), "options", taskMap.get("options"),
								"correctAnswers", taskMap.get("correctAnswers"));
						taskReq.setContent(objectMapper.convertValue(content,
								com.skilltree.dto.content.TaskContent.class));
					}

					taskService.create(taskReq);
				}
			}
		}

		return course.getId();
	}
}