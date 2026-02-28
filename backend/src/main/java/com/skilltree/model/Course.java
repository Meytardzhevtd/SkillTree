package com.skilltree.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Course {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "id_user", nullable = false)
	private User user;

	@Column(nullable = false)
	private String name;

	@Column(nullable = true)
	private String description;

	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL)
	private List<Module> modules;

	// add getters!!!
}