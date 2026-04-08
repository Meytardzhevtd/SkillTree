package com.skilltree.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "username", unique = true, nullable = false)
	private String username;

	@Column(name = "password", nullable = false)
	private String password;

	@Column(name = "email", unique = true, nullable = false)
	private String email;

	@Enumerated(EnumType.STRING)
	@Column(name = "role", nullable = false)
	private Role role = Role.BASE_USER;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
	private List<Avatar> avatars = new ArrayList<>();

	public Users(String username, String email, String password) {
		this.username = username;
		this.email = email;
		this.password = password;
		this.role = Role.BASE_USER;
	}

	public Users(Long id, String username, String email, String password) {
		this.id = id;
		this.username = username;
		this.email = email;
		this.password = password;
		this.role = Role.BASE_USER;
	}
}
