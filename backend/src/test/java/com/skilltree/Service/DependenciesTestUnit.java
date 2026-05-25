package com.skilltree.Service;

import com.skilltree.model.Courses;
import com.skilltree.model.Dependencies;
import com.skilltree.model.Module;
import com.skilltree.repository.DependencyRepository;
import com.skilltree.repository.ModuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DependenciesTestUnit {

    @Mock
    private ModuleRepository moduleRepository;
    @Mock
    private DependencyRepository dependencyRepository;

    @InjectMocks
    private DependenciesService dependenciesService;

    private Courses course;
    private Module moduleA;
    private Module moduleB;
    private Module moduleC;

    @BeforeEach
    void setUp() {
        course = new Courses();
        course.setId(1L);
        moduleA = new Module(1L, course, "A", true);
        moduleB = new Module(2L, course, "B", true);
        moduleC = new Module(3L, course, "C", true);
    }

    @Test
    void makeSelfDependency() {
        boolean result = dependenciesService.makeDependent(1L, 1L);
        assertThat(result).isFalse();
        verify(dependencyRepository, never()).save(any());
    }

    @Test
    void makeDependencyHappy() {
        when(moduleRepository.findById(1L)).thenReturn(Optional.of(moduleA));
        when(moduleRepository.findById(2L)).thenReturn(Optional.of(moduleB));
        when(dependencyRepository.findByMainModuleId(2L)).thenReturn(List.of());

        boolean result = dependenciesService.makeDependent(1L, 2L);
        assertThat(result).isTrue();
        verify(dependencyRepository).save(any());
    }

    @Test
    void makeCycleDependence() {
        when(dependencyRepository.findByMainModuleId(1L)).thenReturn(List.of(new Dependencies(1L, moduleA, moduleB)));
        when(dependencyRepository.findByMainModuleId(2L)).thenReturn(List.of(new Dependencies(2L, moduleB, moduleC)));
        when(dependencyRepository.findByMainModuleId(3L)).thenReturn(new ArrayList<>());
        boolean result = dependenciesService.makeDependent(2L, 1L);
        assertThat(result).isFalse();
        verify(dependencyRepository, never()).save(any());

        result = dependenciesService.makeDependent(3L, 1L);
        assertThat(result).isFalse();
        verify(dependencyRepository, never()).save(any());
    }
}