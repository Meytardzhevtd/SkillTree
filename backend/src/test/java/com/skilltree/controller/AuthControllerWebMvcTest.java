package com.skilltree.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skilltree.model.User;
import com.skilltree.Service.UserService;
import com.skilltree.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void register_whenRequestIsValid_returns201AndCallsService() throws Exception {
        RegisterRequest request = new RegisterRequest("student_1", "student1@example.com", "strong-password");

        when(userService.register(any(RegisterRequest.class)))
                .thenReturn(new User(1L, "student_1", "student1@example.com", "hashed-password"));

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully"));

        ArgumentCaptor<RegisterRequest> captor = ArgumentCaptor.forClass(RegisterRequest.class);
        verify(userService).register(captor.capture());

        RegisterRequest captured = captor.getValue();

        assertThat(captured.getUsername()).isEqualTo("student_1");
        assertThat(captured.getEmail()).isEqualTo("student1@example.com");
        assertThat(captured.getPassword()).isEqualTo("strong-password");
    }
}
