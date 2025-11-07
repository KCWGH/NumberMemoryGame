package com.memorygame.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "game_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String providerId; // Google Sub ID

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String email;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Score> scores;

    @Builder
    public User(String providerId, String name, String email) {
        this.providerId = providerId;
        this.name = name;
        this.email = email;
    }

    // OAuth2 로그인 시 정보 업데이트
    public User update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }
}