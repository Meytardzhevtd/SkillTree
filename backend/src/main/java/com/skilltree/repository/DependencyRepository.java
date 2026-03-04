package com.skilltree.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.skilltree.model.Dependencies;

public interface DependencyRepository extends JpaRepository<Dependencies, Long> {

}
