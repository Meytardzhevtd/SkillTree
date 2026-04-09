package com.skilltree.dto.takeCourse;

import com.skilltree.model.TakenCourses;
import lombok.Data;

// Dto для получения данных из таблицы, чтобы вывести список курсов выбранных
// конкретным пользователем

@Data
public class TakenCourseInfo {
	private Long takenCourseId;
	private Long courseId;
	private Float progress;
	private String name;
	private String description;

	public TakenCourseInfo(TakenCourses takenCourses) {
		this.takenCourseId = takenCourses.getId();
		this.courseId = takenCourses.getCourse().getId();
		this.progress = takenCourses.getProgress();
		this.name = takenCourses.getCourse().getName();
		this.description = takenCourses.getCourse().getDescription();
	}
}
