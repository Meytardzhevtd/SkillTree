@Nested
class LoginEndpointTests {

	@Test
	void login_whenCorrectCredentials_returns200() throws Exception {
		LoginRequest request = new LoginRequest("user@test.com", "password123");

		User mockUser = new User(1L, "user", "user@test.com", "hash");
		mockUser.setRole(com.skilltree.model.Role.BASE_USER);

		when(userService.login(any(LoginRequest.class))).thenReturn(true);
		when(jwtService.generateToken(eq("user@test.com"))).thenReturn("test.jwt.token");
		when(userService.findByEmail(eq("user@test.com"))).thenReturn(mockUser);

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("test.jwt.token"))
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.user.id").value(1))
				.andExpect(jsonPath("$.user.username").value("user"))
				.andExpect(jsonPath("$.user.email").value("user@test.com"))
				.andExpect(jsonPath("$.user.role").value("BASE_USER"));

		verify(userService).login(any(LoginRequest.class));
		verify(jwtService).generateToken("user@test.com");
		verify(userService).findByEmail("user@test.com");
	}

	@Test
	void login_whenIncorrectCredentials_returns400() throws Exception {
		LoginRequest request = new LoginRequest("user@test.com", "wrong-password");

		when(userService.login(any(LoginRequest.class))).thenReturn(false);

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isBadRequest())
				.andExpect(content().string("Invalid credentials"));

		verify(userService).login(any(LoginRequest.class));
		verify(jwtService, never()).generateToken(any(String.class));
		verify(userService, never()).findByEmail(any());
	}
}