package com.skilltree.Service;

import com.skilltree.model.Courses;
import com.skilltree.model.Module;
import com.skilltree.model.Task;
import com.skilltree.model.Users;
import com.skilltree.dto.CreateCourseRequest;
import com.skilltree.repository.CourseRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseService {

	private final CourseRepository courseRepository;
	private final ModuleRepository moduleRepository;
	private final TaskRepository taskRepository;
	private final UserRepository userRepository;

	public CourseService(CourseRepository courseRepository, ModuleRepository moduleRepository,
			TaskRepository taskRepository, UserRepository userRepository) {
		this.courseRepository = courseRepository;
		this.moduleRepository = moduleRepository;
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
	}

	public List<Courses> getCoursesByUserId(Long userId) {
		// использует метод findByUserId в CourseRepository
		return courseRepository.findByUserId(userId);
	}

	public Courses getCourseById(Long id) {
		return courseRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Course not found"));
	}

	public Courses createCourse(Long userId, String name, String description) {
		Users user = userRepository.findById(userId)
				.orElseThrow(() -> new RuntimeException("User not found"));

		Courses course = new Courses();
		course.setName(name);
		course.setDescription(description);

		return courseRepository.save(course);
	}

	@Transactional
	public Courses createFullCourse(CreateCourseRequest request) {
		Users user = userRepository.findById(request.getUserId())
				.orElseThrow(() -> new RuntimeException("User not found"));

		Courses course = new Courses();
		course.setName(request.getName());
		course.setDescription(request.getDescription());

		List<Module> modules = request.getModules() == null
				? List.of()
				: request.getModules().stream().map(mr -> {
					Module module = new Module();
					module.setName(mr.getName());
					module.setCourse(course);

					List<Task> tasks = mr.getTasks() == null
							? List.of()
							: mr.getTasks().stream().map(tr -> {
								Task task = new Task();
								Map<String, Object> content = new HashMap<>();
								content.put("question", "Ваш вопрос здесь");
								content.put("options",
										Arrays.asList("Вариант 1", "Вариант 2", "Вариант 3"));
								content.put("correctAnswer", 0);
								content.put("points", 10);
								task.setContent(content);
								task.setModule(module);
								return task;
							}).collect(Collectors.toList());

					module.setTasks(tasks);
					return module;
				}).collect(Collectors.toList());

		course.setModules(modules);

		return courseRepository.save(course);
	}

	public Module addModuleToCourse(Long courseId, String name) {
		Courses course = courseRepository.findById(courseId)
				.orElseThrow(() -> new RuntimeException("Course not found"));

		Module module = new Module();
		module.setName(name);
		module.setCourse(course);

		return moduleRepository.save(module);
	}

	public Task addTaskToModule(Long moduleId, Map<String, Object> content) {
		Module module = moduleRepository.findById(moduleId)
				.orElseThrow(() -> new RuntimeException("Module not found"));

		Task task = new Task();
		task.setContent(content);
		task.setModule(module);

		return taskRepository.save(task);
	}
}