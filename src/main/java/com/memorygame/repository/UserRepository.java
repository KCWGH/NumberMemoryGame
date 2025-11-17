package com.memorygame.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.memorygame.model.ProviderType;
import com.memorygame.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByProviderAndProviderId(ProviderType provider, String providerId);
}