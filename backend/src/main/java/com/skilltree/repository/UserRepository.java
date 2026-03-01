package com.skilltree.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.skilltree.model.Users;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
	Optional<Users> findByUsername(String username);

	Optional<Users> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByUsername(String username);
}
