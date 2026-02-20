package com.skilltree.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private final String username;
    private final String email;
    private final String password;
}
