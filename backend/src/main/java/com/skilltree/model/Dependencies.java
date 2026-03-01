package com.skilltree.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "Dependences",
		uniqueConstraints = @UniqueConstraint(name = "uq_dependence",
				columnNames = {"id_module", "id_block_module"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dependencies {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_module", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_dependence_module",
					value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Module module;

	@ManyToOne
	@JoinColumn(name = "id_block_module", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_dependence_block_module",
					value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Module block_module;
}
