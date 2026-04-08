package com.skilltree.repository;

import com.skilltree.model.Avatar;
import com.skilltree.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AvatarRepository extends JpaRepository<Avatar, Long> {
	Optional<Avatar> findTopByUserOrderByCreatedAtDesc(Users user);
}