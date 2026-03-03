package com.skilltree.Service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.skilltree.model.Task;
import com.skilltree.repository.TaskRepository;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskrRepository) {
        this.taskRepository = taskrRepository;
    }

    List<Task> getTasksByModuleId(Long moduleId) {
        return new ArrayList<>();
    }
}
