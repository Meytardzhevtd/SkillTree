package com.skilltree.model;

import com.skilltree.dto.content.TaskContent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Tasks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_type_task", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_task_type", value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private TaskTypes task_type;

	@ManyToOne
	@JoinColumn(name = "id_module", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_task_module", value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Module module;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "content", columnDefinition = "JSONB", nullable = false)
	private TaskContent content;

	@OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserAnswers> user_answers = new ArrayList<>();

	@Column(name = "score", nullable = false)
	private Integer score = 10;

	// Конструктор для существующих вызовов (без score)
	public Task(Long id, TaskTypes task_type, Module module, TaskContent content) {
		this.id = id;
		this.task_type = task_type;
		this.module = module;
		this.content = content;
		this.score = 10;
	}
}