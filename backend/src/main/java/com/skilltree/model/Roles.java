package com.skilltree.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
@Entity
// @Table(name = "Roles",
// uniqueConstraints = @UniqueConstraint(name = "uq_role",
// columnNames = {"id_course", "id_user"}))

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Roles {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "course_role", nullable = false)
	private String course_role;

	@ManyToOne
	@JoinColumn(name = "id_user", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_role_user", value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Users user;

	@ManyToOne
	@JoinColumn(name = "id_course", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_role_course", value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Courses course;

}
