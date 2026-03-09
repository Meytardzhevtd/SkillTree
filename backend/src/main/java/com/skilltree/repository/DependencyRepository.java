package com.skilltree.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skilltree.model.Dependencies;

public interface DependencyRepository extends JpaRepository<Dependencies, Long> {
	List<Dependencies> findByModuleId(Long moduleId);
}
