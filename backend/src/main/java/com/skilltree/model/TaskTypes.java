package com.skilltree.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "TaskTypes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskTypes {
	@Id
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;
	/**
	 * По имени надо наверное обращатся: ONE_POSSIBLE_ANSWER -- один вариант ответа
	 */

	@OneToMany(mappedBy = "task_type", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Task> tasks = new ArrayList<>();
}