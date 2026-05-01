package com.skilltree.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_task_scores",
		uniqueConstraints = @UniqueConstraint(name = "uq_user_task",
				columnNames = {"user_id", "task_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTaskScores {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "task_id", nullable = false)
	private Long taskId;
}