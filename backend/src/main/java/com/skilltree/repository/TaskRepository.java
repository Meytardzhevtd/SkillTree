package com.skilltree.repository;

import com.skilltree.model.Task;
import com.skilltree.model.Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByModule(Module module);
}