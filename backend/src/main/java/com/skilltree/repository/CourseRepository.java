package com.skilltree.repository;

import com.skilltree.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
	List<Course> findByUserId(Long userId);
}
