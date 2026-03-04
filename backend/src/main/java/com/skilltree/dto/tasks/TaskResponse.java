package com.skilltree.dto.tasks;

import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private Long taskTypeId;
    private Long moduleId;
    private Map<String, Object> content;
}
