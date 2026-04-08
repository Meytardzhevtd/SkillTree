package com.skilltree.repository;

import com.skilltree.model.Courses;
import com.skilltree.model.Users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Courses, Long> {
	@Query("SELECT c FROM Courses c JOIN c.roles r WHERE r.user.id = :userId AND r.course_role = :role")
	List<Courses> findByUserIdAndRole(@Param("userId") Long userId, @Param("role") String role);

	@Query("SELECT c FROM Courses c JOIN c.roles r WHERE r.user = :user")
	List<Courses> findByUser(@Param("user") Users user);
}
