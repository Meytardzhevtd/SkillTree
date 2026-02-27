package com.skilltree.dto;

import com.skilltree.model.Course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseDto {
    private Long id;
    private String name;
    private String description;

    public static CourseDto fromEntity(Course c) {
        return new CourseDto(c.getId(), c.getName(), c.getDescription());
    }
}
