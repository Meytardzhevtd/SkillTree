package com.skilltree.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skilltree.model.TakenCourses;
import com.skilltree.model.Users;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TakenCoursesRepository extends JpaRepository<TakenCourses, Long> {
	@Query("SELECT EXISTS (SELECT 1 FROM TakenCourses t WHERE t.course.id = :courseId AND t.user.id = :userId)")
	boolean existsByCourseIdAndUserId(@Param("courseId") Long courseId,
			@Param("userId") Long userId);
	List<TakenCourses> findByUser(Users user);
	List<TakenCourses> findByUserId(Long userId);
}
