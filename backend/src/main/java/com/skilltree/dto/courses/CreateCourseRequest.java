package com.skilltree.dto.courses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateCourseRequest {
    private String name;
    private String description;
}
