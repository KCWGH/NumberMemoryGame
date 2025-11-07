package com.memorygame.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "game_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    // Google OAuth2에서 제공하는 고유 ID (sub)를 기본 키로 사용
    @Id
    private String id;
    
    private String email;
    
    private String name;
}