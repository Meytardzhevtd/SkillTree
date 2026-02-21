package com.skilltree.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skilltree.Model.User;
import com.skilltree.repository.UserRepository;
import java.util.Optional;
import com.skilltree.dto.*;

public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        User newUser = new User(user.getUsername(), user.getEmail(), passwordEncoder.encode(user.getPassword()));
        return userRepository.save(newUser);
    }

    public boolean login(LoginRequest loginRequest) {
        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
        if (user.isPresent()) {
            return passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword());
        } else {
            return false;
        }
    }
}
