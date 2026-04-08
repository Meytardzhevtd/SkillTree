package com.skilltree.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "takencourses",
		uniqueConstraints = @UniqueConstraint(name = "uq_taken_course",
				columnNames = {"id_course", "id_user"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TakenCourses {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "progress", nullable = false)
	private Float progress = 0.0F;

	@ManyToOne
	@JoinColumn(name = "id_course", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_taken_courses_course",
					value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Courses course;

	@ManyToOne
	@JoinColumn(name = "id_user", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_taken_courses_user",
					value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Users user;

	@OneToMany(mappedBy = "taken_courses", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProgressModule> progress_modules = new ArrayList<>();
}
