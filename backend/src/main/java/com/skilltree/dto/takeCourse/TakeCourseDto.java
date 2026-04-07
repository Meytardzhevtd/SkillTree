package com.skilltree.dto.takeCourse;

import lombok.Data;

// Dto для отправки на сервер информации о выбранном курсе

@Data
public class TakeCourseDto {
	private Long id;
	private Long courseId;
	private Long userId;
	private String role;
}
