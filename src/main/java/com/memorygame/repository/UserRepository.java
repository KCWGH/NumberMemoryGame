package com.memorygame.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.memorygame.model.User;

public interface UserRepository extends JpaRepository<User, String> {
    // ID (sub) 기반으로 User를 찾는 기본 JpaRepository 기능 사용
}