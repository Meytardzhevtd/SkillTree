package com.skilltree.dto;

import com.skilltree.model.Role;

public record ProfileResponse(Long id, String username, String email, Role role) {
}
