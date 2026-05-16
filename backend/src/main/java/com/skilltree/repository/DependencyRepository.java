package com.skilltree.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skilltree.model.Dependencies;

public interface DependencyRepository extends JpaRepository<Dependencies, Long> {

	@Query("SELECT d FROM Dependencies d WHERE d.mainModule.id = :moduleId")
	List<Dependencies> findByMainModuleId(@Param("moduleId") Long moduleId);

	@Query("SELECT d FROM Dependencies d WHERE d.blockedModule.id = :moduleId")
	List<Dependencies> findByBlockedModuleId(@Param("moduleId") Long moduleId);

	@Query("SELECT d FROM Dependencies d WHERE d.mainModule.id = :mainModuleId AND d.blockedModule.id = :blockModuleId")
	Dependencies findByMainModuleIdAndBlockedModuleId(@Param("mainModuleId") Long mainModuleId,
			@Param("blockModuleId") Long blockModuleId);
}