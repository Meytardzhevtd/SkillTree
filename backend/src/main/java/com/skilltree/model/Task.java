package com.skilltree.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// связь к модулю (id_module)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_module", nullable = false)
	private Module module;

	@Column(columnDefinition = "text", nullable = false)
	private String content;

	// ======= геттеры и сеттеры =======
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}