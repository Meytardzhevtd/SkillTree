package com.skilltree.repository;

import java.util.List;

import com.skilltree.model.Module;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для Module. Добавлены методы для удобного получения модулей по
 * курсу.
 */
public interface ModuleRepository extends JpaRepository<Module, Long> {

	boolean existsById(Long id);

	/**
	 * Получить все модули курса по id курса, упорядоченные по id.
	 *
	 * @param courseId
	 *            id курса (Module.course.id)
	 * @return список модулей курса
	 */
	List<Module> findByCourseIdOrderById(Long courseId);
}