package com.skilltree.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "UserAnswers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswers {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_task", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_answer_task", value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Task task;

	@ManyToOne
	@JoinColumn(name = "id_progress_module", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_answer_progress",
					value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private ProgressModule progress_module;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "answer", columnDefinition = "JSONB", nullable = false)
	private Map<String, Object> answer;

	@Column(name = "is_correct", nullable = false)
	private boolean isCorrect;
}
