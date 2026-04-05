// package com.skilltree.Service;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.never;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.Optional;

// import org.junit.jupiter.api.AfterEach;
// import org.junit.jupiter.api.Disabled;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import
// org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;

// import com.skilltree.dto.module.ModuleResponse;
// import com.skilltree.model.Module;
// import com.skilltree.model.ProgressModule;
// import com.skilltree.model.TakenCourses;
// import com.skilltree.model.Task;
// import com.skilltree.model.UserAnswers;
// import com.skilltree.model.Users;
// import com.skilltree.repository.CourseRepository;
// import com.skilltree.repository.DependencyRepository;
// import com.skilltree.repository.ModuleRepository;
// import com.skilltree.repository.ProgressModuleRepository;
// import com.skilltree.repository.UserRepository;

// @Disabled
// @ExtendWith(MockitoExtension.class)
// public class ModuleServiceTest {

// @Mock
// private ModuleRepository moduleRepository;

// @Mock
// private CourseRepository courseRepository;

// @Mock
// private DependencyRepository dependencyRepository;

// @Mock
// private ProgressModuleRepository progressModuleRepository;

// @Mock
// private UserRepository userRepository;

// @InjectMocks
// private ModuleService moduleService;

// @AfterEach
// void clearSecurityContext() {
// SecurityContextHolder.clearContext();
// }

// @Test
// void getModule_whenTaskHasAnswerOfCurrentUser_shouldMarkTaskCompleted() {
// Users currentUser = new Users();
// currentUser.setId(101L);

// SecurityContextHolder.getContext()
// .setAuthentication(new UsernamePasswordAuthenticationToken("user@mail.com",
// "pwd",
// List.of(new SimpleGrantedAuthority("ROLE_USER"))));
// when(userRepository.findByEmail("user@mail.com")).thenReturn(Optional.of(currentUser));

// Module module = new Module(5L, null, "Module", true);
// Task task = new Task();
// task.setId(11L);

// TakenCourses takenCourses = new TakenCourses();
// takenCourses.setUser(currentUser);

// ProgressModule progressModule = new ProgressModule();
// progressModule.setTaken_courses(takenCourses);

// UserAnswers userAnswer = new UserAnswers(1L, task, progressModule,
// Map.of("a", "b"));
// task.setUser_answers(List.of(userAnswer));
// module.setTasks(List.of(task));

// when(moduleRepository.findById(5L)).thenReturn(Optional.of(module));

// ModuleResponse response = moduleService.getModule(5L);

// assertNotNull(response);
// assertEquals(1, response.tasks().size());
// assertEquals(11L, response.tasks().get(0).taskId());
// assertTrue(response.tasks().get(0).isCompleted());
// }

// @Test
// void getModule_whenNoAuthenticatedUser_shouldKeepTasksIncomplete() {
// Module module = new Module(6L, null, "Module", true);
// Task task = new Task();
// task.setId(22L);
// task.setUser_answers(new ArrayList<>());
// module.setTasks(List.of(task));

// when(moduleRepository.findById(6L)).thenReturn(Optional.of(module));

// ModuleResponse response = moduleService.getModule(6L);

// assertNotNull(response);
// assertEquals(1, response.tasks().size());
// assertFalse(response.tasks().get(0).isCompleted());
// verify(userRepository, never()).findByEmail(anyString());
// }
// }
