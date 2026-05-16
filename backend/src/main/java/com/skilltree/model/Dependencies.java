package com.skilltree.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "Dependencies",
		uniqueConstraints = @UniqueConstraint(name = "uq_dependence",
				columnNames = {"id_main_module", "id_blocked_module"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dependencies {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_main_module", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_dependence_main_module ",
					value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Module mainModule;

	@ManyToOne
	@JoinColumn(name = "id_blocked_module", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_dependence_blocked_module",
					value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Module blockedModule;
}
