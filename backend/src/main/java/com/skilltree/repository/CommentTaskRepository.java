package com.skilltree.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.skilltree.model.CommentTask;

public interface CommentTaskRepository extends JpaRepository<CommentTask, Long> {
	List<CommentTask> findByTaskIdOrderByCreatedAtAsc(Long taskId);
}