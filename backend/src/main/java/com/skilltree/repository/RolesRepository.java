package com.skilltree.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skilltree.model.Roles;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolesRepository extends JpaRepository<Roles, Long> {
	@Query("SELECT EXISTS (SELECT 1 FROM TakenCourses t WHERE t.course.id = :courseId AND t.user.id = :userId)")
	boolean existsByCourseIdAndUserId(@Param("courseId") Long courseId,
			@Param("userId") Long userId);
}
