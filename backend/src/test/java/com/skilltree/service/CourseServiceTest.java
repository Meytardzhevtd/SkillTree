package com.skilltree.service;

import com.skilltree.Service.CourseService;
import com.skilltree.model.Courses;
import com.skilltree.model.Users;
import com.skilltree.repository.CourseRepository;
import com.skilltree.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

	@Mock
	private CourseRepository courseRepository;
	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private CourseService courseService;

	@Test
	void shouldCreateCourse() {
		Long userId = 1L;
		String name = "Java Basics";
		String description = "Learn Java from scratch";

		Users user = new Users();
		user.setId(userId);
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		courseService.createCourse(userId, name, description);

		ArgumentCaptor<Courses> courseCaptor = ArgumentCaptor.forClass(Courses.class);
		verify(courseRepository, times(1)).save(courseCaptor.capture());
		Courses savedCourse = courseCaptor.getValue();

		assertNull(savedCourse.getId(), "ID должен быть null перед сохранением");
		// assertEquals(userId, savedCourse.getUser().getId(), "UserId должен совпадать
		// с переданным");
		assertEquals(name, savedCourse.getName(), "Название курса должно совпадать");
		assertEquals(description, savedCourse.getDescription(), "Описание курса должно совпадать");
	}

	@Test
	void shouldReturnCoursesForUser() {
		Long userId = 1L;

		Users user = new Users();
		user.setId(userId);

		Courses course1 = new Courses();
		course1.setId(1L);
		course1.setName("Java");
		course1.setDescription("Basics");

		Courses course2 = new Courses();
		course2.setId(2L);
		course2.setName("Spring");
		course2.setDescription("Advanced");

		List<Courses> mockCourses = Arrays.asList(course1, course2);

		when(courseRepository.findByUserId(userId)).thenReturn(mockCourses);

		List<Courses> courses = courseService.getCoursesByUserId(userId);

		assertNotNull(courses, "Список курсов не должен быть null");
		assertEquals(2, courses.size(), "Должно быть 2 курса");
		assertEquals("Java", courses.get(0).getName(), "Первый курс должен называться Java");
		assertEquals("Spring", courses.get(1).getName(), "Второй курс должен называться Spring");

		verify(courseRepository, times(1)).findByUserId(userId);
	}
}