package com.skilltree.Service;

import com.skilltree.SkillTreeApplication;
import com.skilltree.model.*;
import com.skilltree.model.Module;
import com.skilltree.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = SkillTreeApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Testcontainers
@Transactional
public class DependencyTestIntegration {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.jpa.show-sql", () -> "false");
        registry.add("logging.level.org.hibernate.SQL", () -> "INFO");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ModuleRepository moduleRepository;
    @Autowired
    private DependencyRepository dependencyRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TakenCoursesRepository takenCoursesRepository;
    @Autowired
    private ProgressModuleRepository progressModuleRepository;

    @BeforeEach
    void setUp() {
        Users user = new Users("tester", "111@222", "encoded");
        user = userRepository.save(user);
    }

    @Test
//    @Timeout(value=5, unit=SECONDS)
    void testGraphEndpointPerformance() throws Exception {
        Courses course = new Courses();
        course.setName("Test");
        course.setDescription("Test");
        course = courseRepository.save(course);

        List<Module> modules = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            Module m = new Module(null, course, "Module " + i, true);
            moduleRepository.save(m);
            modules.add(m);
        }

        for (int i = 0; i < modules.size() - 1; i++) {
            Dependencies dep = new Dependencies();
            dep.setMainModule(modules.get(i));
            dep.setBlockedModule(modules.get(i + 1));
            dependencyRepository.save(dep);
        }

        Users user = userRepository.findAll().get(0);
        TakenCourses taken = new TakenCourses();
        taken.setCourse(course);
        taken.setUser(user);
        taken = takenCoursesRepository.save(taken);

        for (Module m : modules) {
            ProgressModule pm = new ProgressModule();
            pm.setModule(m);
            pm.setTaken_courses(taken);
            pm.setProgress(0f);
            progressModuleRepository.save(pm);
        }

        long start = System.currentTimeMillis();
        mockMvc.perform(get("/api/dependencies/graph/takenCourse/{takenCourse}", taken.getId()))
                .andExpect(status().isOk());
        long duration = System.currentTimeMillis() - start;

        System.out.println("Time: " + duration + " ms");
        assert duration < 10000 : "Time: " + duration + " ms";
    }

    @Test
    void testGraphWithManyDependencies() throws Exception {
        Courses course = new Courses();
        course.setName("Test");
        course.setDescription("Test");
        course = courseRepository.save(course);

        List<Module> modules = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Module m = new Module(null, course, "Module " + i, true);
            modules.add(moduleRepository.save(m));
        }

        for (int i = 0; i < modules.size(); i++) {
            for (int j = i + 1; j < modules.size(); j++) {
                Dependencies dep = new Dependencies();
                dep.setMainModule(modules.get(i));
                dep.setBlockedModule(modules.get(j));
                dependencyRepository.save(dep);
            }
        }

        Users user = userRepository.findAll().get(0);
        TakenCourses taken = new TakenCourses();
        taken.setCourse(course);
        taken.setUser(user);
        taken = takenCoursesRepository.save(taken);

        for (Module m : modules) {
            ProgressModule pm = new ProgressModule();
            pm.setModule(m);
            pm.setTaken_courses(taken);
            pm.setProgress(0f);
            progressModuleRepository.save(pm);
        }

        long start = System.currentTimeMillis();
        mockMvc.perform(get("/api/dependencies/graph/takenCourse/{takenCourse}", taken.getId()))
                .andExpect(status().isOk());
        long duration = System.currentTimeMillis() - start;

        System.out.println("Time: " + duration + " ms");
        assert duration < 10000 : "Time: " + duration + " ms";
    }
}