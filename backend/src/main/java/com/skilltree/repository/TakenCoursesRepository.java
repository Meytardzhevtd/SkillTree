package com.skilltree.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.skilltree.model.TakenCourses;
import com.skilltree.model.Users;

public interface TakenCoursesRepository extends JpaRepository<TakenCourses, Long> {

	List<TakenCourses> findByUser(Users user);
}
