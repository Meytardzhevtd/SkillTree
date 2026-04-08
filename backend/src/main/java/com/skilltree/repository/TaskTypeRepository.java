package com.skilltree.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skilltree.model.TaskTypes;

public interface TaskTypeRepository extends JpaRepository<TaskTypes, Long> {

	/**
	 * Найти тип задания по имени (без учёта регистра).
	 *
	 * @param name
	 *            имя типа
	 * @return Optional с найденной сущностью
	 */
	Optional<TaskTypes> findByNameIgnoreCase(String name);

	Optional<TaskTypes> findById(Long id);
}
