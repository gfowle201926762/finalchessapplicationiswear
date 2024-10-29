package com.chess.application.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import jakarta.persistence.GenerationType;

@Entity
@Data
@Table
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String username;
    private String password;
    private List<Game> games;
}
