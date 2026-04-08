package com.skilltree.dto.courses;

/**
 * dto для того чтобы получать список курсов. То есть мы по user_id получаем
 * список курсов, который он проходит. Можно перейти на курс, нажав на него и
 * увидеть список модулей
 */

public record CourseSimpleDto(Long courseId, String title, String description) {
}
