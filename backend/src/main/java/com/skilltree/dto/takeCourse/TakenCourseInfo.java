package com.skilltree.dto.takeCourse;

import com.skilltree.model.TakenCourses;
import lombok.Data;

import java.util.List;

@Data
public class TakenCourseInfo {
	private Long takenCourseId;
	private Long courseId;
	private Float progress;
	private String name;
	private String description;

	private List<ModuleProgressInfo> moduleProgresses;

	public TakenCourseInfo(TakenCourses takenCourses) {
		this.takenCourseId = takenCourses.getId();
		this.courseId = takenCourses.getCourse().getId();
		this.progress = takenCourses.getProgress();
		this.name = takenCourses.getCourse().getName();
		this.description = takenCourses.getCourse().getDescription();
		this.moduleProgresses = takenCourses.getProgress_modules().stream()
				.map(ModuleProgressInfo::new).toList();
	}
}