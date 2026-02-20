package com.skilltree.Service;

import java.lang.foreign.Linker.Option;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.skilltree.Model.User;
import com.skilltree.repository.UserRepository;
import java.util.Optional;

public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Ползователь с таким email уже существует");
        } else {
            User user = User(username, email, password);
            return userRepository.save(user);
        }
    }

    public Optional<User> getUserById(Long id) {
        if (userRepository.existsById(id)) {
            return userRepository.findById(id);
        } else {
            throw new IllegalArgumentException("Пользователь с таким id не существует");
        }
    }
}
