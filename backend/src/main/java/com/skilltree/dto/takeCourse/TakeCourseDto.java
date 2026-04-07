package com.skilltree.dto.takeCourse;

import lombok.Data;

@Data
public class TakeCourseDto {
	private Long id;
	private Long courseId;
	private Long userId;
	private String role;
}
