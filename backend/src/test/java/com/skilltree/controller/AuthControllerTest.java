package com.skilltree.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilltree.model.User;
import com.skilltree.Service.JwtService;
import com.skilltree.Service.UserService;
import com.skilltree.dto.LoginRequest;
import com.skilltree.dto.RegisterRequest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	@MockBean
	private JwtService jwtService;

	@Nested
	class RegisterEndpointTests {

		@Test
		void register_whenValidData_returns201() throws Exception {
			RegisterRequest request = new RegisterRequest("user", "user@test.com", "password123");
			when(userService.register(any()))
					.thenReturn(new User(1L, "user", "user@test.com", "hash"));

			mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(content().string("User registered successfully"));

			verify(userService).register(any(RegisterRequest.class));
		}

		@Test
		void register_whenServiceThrows_returns400WithMessage() throws Exception {
			RegisterRequest request = new RegisterRequest("user", "user@test.com", "123");

			when(userService.register(any()))
					.thenThrow(new RuntimeException("Email already exists"));

			mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isBadRequest())
					.andExpect(content().string("Email already exists"));
		}
	}

	@Nested
	class LoginEndpointTests {
		@Test
		void login_whenCorrectCredentials_returns200() throws Exception {
			LoginRequest request = new LoginRequest("user@test.com", "password123");
			when(userService.login(any(LoginRequest.class))).thenReturn(true);
			when(jwtService.generateToken(eq("user@test.com"))).thenReturn("test.jwt.token");

			mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
					.andExpect(jsonPath("$.token").value("test.jwt.token"))
					.andExpect(jsonPath("$.tokenType").value("Bearer"));

			verify(userService).login(any(LoginRequest.class));
			verify(jwtService).generateToken("user@test.com");
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
		}
	}
}
