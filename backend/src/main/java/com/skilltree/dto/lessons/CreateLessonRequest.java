package com.skilltree.dto.lessons;

import jakarta.validation.constraints.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CreateLessonRequest {
    @NotNull
    private Long moduleId;
    @NotBlank
    private String title;
    @NotBlank
    private String content;
}
