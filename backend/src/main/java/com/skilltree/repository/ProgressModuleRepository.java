package com.skilltree.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skilltree.model.ProgressModule;

/**
 * Репозиторий для ProgressModule.
 * Добавлен метод для поиска прогресса по module.id и taken_courses.id (конкретное прохождение пользователя).
 */
public interface ProgressModuleRepository extends JpaRepository<ProgressModule, Long> {

	/**
	 * Найти ProgressModule по id модуля и id записи TakenCourses.
	 * Используется при проверке зависимостей (нужно узнать прогресс блокирующего модуля для конкретного takenCourse).
	 *
	 * @param moduleId id модуля (ProgressModule.module.id)
	 * @param takenCourseId id записи TakenCourses (ProgressModule.taken_courses.id)
	 * @return Optional с найденной записью
	 */
	@Query("SELECT pm FROM ProgressModule pm WHERE pm.module.id = :moduleId AND pm.taken_courses.id = :takenCourseId")
	Optional<ProgressModule> findByModuleIdAndTakenCoursesId(@Param("moduleId") Long moduleId,
															 @Param("takenCourseId") Long takenCourseId);
}
