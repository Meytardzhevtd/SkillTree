package com.skilltree.dto.tasks;

import java.util.Map;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTaskDto {
    private Long taskTypeId;
    private Map<String, Object> content;
}
