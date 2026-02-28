package com.skilltree.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
	private String token;
	private String tokenType = "Bearer";
	private UserInfo user;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class UserInfo {
		private Long id;
		private String username;
		private String email;
		private String role;
	}
}
