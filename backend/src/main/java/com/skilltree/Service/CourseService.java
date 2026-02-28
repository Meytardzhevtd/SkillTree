package com.skilltree.Service;

import com.skilltree.model.Course;
import com.skilltree.model.Module;
import com.skilltree.model.Task;
import com.skilltree.model.User;
import com.skilltree.dto.CreateCourseRequest;
import com.skilltree.dto.CreateModuleRequest;
import com.skilltree.dto.CreateTaskRequest;
import com.skilltree.repository.CourseRepository;
import com.skilltree.repository.ModuleRepository;
import com.skilltree.repository.TaskRepository;
import com.skilltree.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseService {

	private final CourseRepository courseRepository;
	private final ModuleRepository moduleRepository;
	private final TaskRepository taskRepository;
	private final UserRepository userRepository;

	public CourseService(CourseRepository courseRepository,
						 ModuleRepository moduleRepository,
						 TaskRepository taskRepository,
						 UserRepository userRepository) {
		this.courseRepository = courseRepository;
		this.moduleRepository = moduleRepository;
		this.taskRepository = taskRepository;
		this.userRepository = userRepository;
	}

	public List<Course> getCoursesByUserId(Long userId) {
		User user = userRepository.findById(userId).orElseThrow();
		return courseRepository.findAll().stream()
				.filter(c -> c.getUser().getId().equals(user.getId()))
				.collect(Collectors.toList());
	}

	public Course getCourseById(Long id) {
		return courseRepository.findById(id).orElseThrow();
	}

	public Course createCourse(Long userId, String name, String description) {
		User user = userRepository.findById(userId).orElseThrow();

		Course course = new Course();
		course.setUser(user);
		course.setName(name);
		course.setDescription(description);

		return courseRepository.save(course);
	}

	@Transactional
	public Course createFullCourse(CreateCourseRequest request) {
		User user = userRepository.findById(request.getUserId()).orElseThrow();

		Course course = new Course();
		course.setUser(user);
		course.setName(request.getName());
		course.setDescription(request.getDescription());

		List<Module> modules = request.getModules().stream().map(mr -> {
			Module module = new Module();
			module.setName(mr.getName());
			module.setCourse(course);

			List<Task> tasks = mr.getTasks().stream().map(tr -> {
				Task task = new Task();
				task.setContent(tr.getContent());
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
		Course course = courseRepository.findById(courseId).orElseThrow();

		Module module = new Module();
		module.setName(name);
		module.setCourse(course);

		return moduleRepository.save(module);
	}

	public Task addTaskToModule(Long moduleId, String content) {
		Module module = moduleRepository.findById(moduleId).orElseThrow();

		Task task = new Task();
		task.setContent(content);
		task.setModule(module);

		return taskRepository.save(task);
	}
}