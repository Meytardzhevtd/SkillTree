package com.skilltree.dto.comments;

import java.time.LocalDateTime;

import com.skilltree.dto.UserSimpleDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentTaskResponse {
	private Long id;
	private String text;
	private Long taskId;
	private UserSimpleDto author;
	private LocalDateTime createdAt;
	private Boolean isEdited;
}