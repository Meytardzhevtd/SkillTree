package com.skilltree.dto.module;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CreateModuleRequest {
    private Long courseId;
    private String name;
    private Boolean can_be_open;
}
