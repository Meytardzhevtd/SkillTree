package com.skilltree.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skilltree.model.UserTaskScores;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserTaskScoresRepository extends JpaRepository<UserTaskScores, Long> {

	boolean existsByUserIdAndTaskId(Long userId, Long taskId);

	@Query("SELECT uts FROM UserTaskScores uts WHERE uts.userId = :userId AND uts.taskId = :taskId")
	Optional<UserTaskScores> findByUserIdAndTaskId(@Param("userId") Long userId,
			@Param("taskId") Long taskId);

	@Query("SELECT SUM(t.score) FROM UserTaskScores uts JOIN Task t ON t.id = uts.taskId WHERE uts.userId = :userId")
	Integer getTotalScoreByUserId(@Param("userId") Long userId);

}
