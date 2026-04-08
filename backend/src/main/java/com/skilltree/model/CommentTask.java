package com.skilltree.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments_task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentTask {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String text;

	@ManyToOne
	@JoinColumn(name = "task_id", nullable = false)
	private Task task;

	@ManyToOne
	@JoinColumn(name = "author_id", nullable = false)
	private Users author;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "is_edited")
	private Boolean isEdited = false;
}