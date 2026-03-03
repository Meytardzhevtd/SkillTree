package com.skilltree.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CreateModuleDto {
    private Long courseId;
    private String name;
}
