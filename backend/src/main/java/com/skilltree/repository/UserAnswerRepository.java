package com.skilltree.repository;

import com.skilltree.model.ProgressModule;
import com.skilltree.model.Task;
import com.skilltree.model.UserAnswers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswers, Long> {

	@Query("SELECT COUNT(ua) > 0 FROM UserAnswers ua "
			+ "WHERE ua.task = :task AND ua.progress_module = :progressModule AND ua.isCorrect = true")
	boolean existsCorrectAnswerByTaskAndProgressModule(@Param("task") Task task,
			@Param("progressModule") ProgressModule progressModule);

	@Query("SELECT ua FROM UserAnswers ua WHERE ua.progress_module = :progressModule")
	List<UserAnswers> findByProgressModule(@Param("progressModule") ProgressModule progressModule);

	@Query("SELECT COUNT(DISTINCT ua.task.id) FROM UserAnswers ua "
			+ "WHERE ua.progress_module = :progressModule AND ua.isCorrect = true")
	long countDistinctCorrectTasksByProgressModule(
			@Param("progressModule") ProgressModule progressModule);
}
