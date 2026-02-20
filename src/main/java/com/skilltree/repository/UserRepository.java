package com.skilltree.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.skilltree.Model.User;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
