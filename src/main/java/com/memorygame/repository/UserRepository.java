package com.memorygame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.memorygame.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByGoogleId(String googleId);
}
