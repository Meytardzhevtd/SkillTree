package com.skilltree.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Module")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Module {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@ManyToOne
	@JoinColumn(name = "id_course", referencedColumnName = "id", nullable = false,
			foreignKey = @ForeignKey(name = "fk_module_course", value = ConstraintMode.CONSTRAINT))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Courses course;

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "can_be_open", nullable = false)
	private Boolean can_be_open;

	@OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Task> tasks = new ArrayList<>();

	@OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Dependencies> dependencies = new ArrayList<>();

	@OneToMany(mappedBy = "block_module", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Dependencies> blockedBy = new ArrayList<>();

	@OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProgressModule> progressModules = new ArrayList<>();

	public Module(Courses course, String name, Boolean can_be_open) {
		this.course = course;
		this.name = name;
		this.can_be_open = can_be_open;
	}
}