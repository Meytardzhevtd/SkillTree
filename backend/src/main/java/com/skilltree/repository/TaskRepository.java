package com.skilltree.repository;

import com.skilltree.model.Task;
import com.skilltree.model.Module;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
	List<Task> findByModule(Module module);

	@Query("SELECT COALESCE(SUM(t.score), 0) FROM Task t WHERE t.module = :module")
	long sumScoreByModule(@Param("module") Module module);
}