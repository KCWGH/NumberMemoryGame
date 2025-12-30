package com.memorygame.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "game_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"provider", "providerId"}) 
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderType provider; 

    @Column(nullable = false)
    private String providerId; 

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String email;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Score> scores;

    @Builder
    public User(ProviderType provider, String providerId, String name, String email) {
        this.provider = provider;
        this.providerId = providerId;
        this.name = name;
        this.email = email;
    }

    public User update(String name, String email) {
        this.name = name;
        this.email = email;
        return this;
    }
}