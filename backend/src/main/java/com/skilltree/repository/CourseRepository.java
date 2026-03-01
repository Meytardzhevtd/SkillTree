package com.skilltree.repository;

import com.skilltree.model.Courses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Courses, Long> {
	List<Courses> findByUserId(Long userId);
}
