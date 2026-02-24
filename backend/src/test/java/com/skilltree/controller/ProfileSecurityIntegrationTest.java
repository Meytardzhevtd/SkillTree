package com.skilltree.controller;

import com.skilltree.Service.UserService;
import com.skilltree.Service.JwtService;
import com.skilltree.config.JwtAuthenticationFilter;
import com.skilltree.config.SecurityConfig;
import com.skilltree.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc
class ProfileSecurityIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private UserService userService;

	@MockBean
	private JwtService jwtService;

	@Test
	void profileMe_withoutToken_returnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/profile/me")).andExpect(status().isForbidden());
	}

	@Test
	void profileMe_withValidToken_returnsProfile() throws Exception {
		String email = "student1@example.com";
		String token = "valid.jwt.token";

		when(jwtService.extractEmail(token)).thenReturn(email);
		when(jwtService.isTokenValid(token, email)).thenReturn(true);
		when(userService.findByEmail(email))
				.thenReturn(new User(1L, "student_1", email, "hashed-password"));

		mockMvc.perform(get("/api/profile/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.username").value("student_1"));
	}

	@Test
	void profileMe_withInvalidToken_returnsUnauthorized() throws Exception {
		String token = "invalid.jwt.token";

		when(jwtService.extractEmail(token)).thenReturn("student1@example.com");
		when(jwtService.isTokenValid(anyString(), anyString())).thenReturn(false);

		mockMvc.perform(get("/api/profile/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());
	}
}
