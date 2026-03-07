package com.skilltree.repository;

import com.skilltree.model.Courses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Courses, Long> {
	@Query("SELECT c FROM Courses c JOIN c.takenCourses tc WHERE tc.user.id = :userId")
	List<Courses> findByUserId(Long userId);
}
