package com.skilltree.dto.courses;

import com.skilltree.model.Courses;

/**
 * dto для того чтобы получать список курсов. То есть мы по user_id получаем
 * список курсов, который он проходит. Можно перейти на курс, нажав на него и
 * увидеть список модулей
 */

public record CourseSimpleDto(Long courseId, String title, String description) {
	public CourseSimpleDto(Courses course) {
		this(course.getId(), course.getName(), course.getDescription());
	}
}
