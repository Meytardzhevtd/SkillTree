package com.skilltree.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "progressmodule",
		uniqueConstraints = @UniqueConstraint(name = "uq_progress",
				columnNames = {"id_module", "id_taken_course"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressModule {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_module", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_progress_module",
					value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Module module;

	@ManyToOne
	@JoinColumn(name = "id_taken_course", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_progress_taken_course",
					value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private TakenCourses taken_courses;

	@Column(name = "progress", nullable = false)
	private Float progress;

	@OneToMany(mappedBy = "progress_module", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<UserAnswers> user_answers = new ArrayList<>();

}
