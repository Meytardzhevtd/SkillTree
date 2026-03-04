package com.skilltree.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skilltree.model.TakenCourses;

/**
 * Репозиторий для TakenCourses.
 * Полезный метод: найти запись прохождения курса по userId и courseId.
 */
public interface TakenCoursesRepository extends JpaRepository<TakenCourses, Long> {

    /**
     * Найти запись TakenCourses по id пользователя и id курса.
     * Соответствует полям TakenCourses.user.id и TakenCourses.course.id.
     *
     * @param userId id пользователя
     * @param courseId id курса
     * @return Optional с найденной записью (уникальность гарантирована uq_taken_course)
     */
    Optional<TakenCourses> findByUserIdAndCourseId(Long userId, Long courseId);
}
