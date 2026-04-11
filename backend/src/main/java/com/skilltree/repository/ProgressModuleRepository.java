package com.skilltree.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skilltree.model.ProgressModule;

public interface ProgressModuleRepository extends JpaRepository<ProgressModule, Long> {
	@Query("SELECT pm FROM ProgressModule pm WHERE pm.module.id = :moduleId AND pm.taken_courses.id = :takenCourseId")
	Optional<ProgressModule> findByModuleIdAndTakenCoursesId(@Param("moduleId") Long moduleId,
			@Param("takenCourseId") Long takenCourseId);

	@Query("SELECT pm FROM ProgressModule pm WHERE pm.module.id = :moduleId")
	List<ProgressModule> findByModuleId(@Param("moduleId") Long moduleId);
}
