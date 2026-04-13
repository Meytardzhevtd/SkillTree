package com.skilltree.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skilltree.model.Dependencies;

public interface DependencyRepository extends JpaRepository<Dependencies, Long> {

	@Query("SELECT d FROM Dependencies d WHERE d.module.id = :moduleId")
	List<Dependencies> findByModuleId(@Param("moduleId") Long moduleId);

	@Query("SELECT d FROM Dependencies d WHERE d.block_module.id = :moduleId")
	List<Dependencies> findByBlockModuleId(@Param("moduleId") Long moduleId);

	@Query("SELECT d FROM Dependencies d WHERE d.module.id = :mainModuleId AND d.block_module.id = :blockModuleId")
	Dependencies findByModuleIdAndBlockModuleId(@Param("mainModuleId") Long mainModuleId,
			@Param("blockModuleId") Long blockModuleId);
}