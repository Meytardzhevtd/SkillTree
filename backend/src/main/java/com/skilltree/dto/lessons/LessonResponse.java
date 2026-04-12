package com.skilltree.dto.lessons;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LessonResponse {
    private Long id;
    private String title;
    private String content;
}
