package com.chess.application.controller.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Embeddable
public class Settings {
    long breadth;
    long engineColour;
    long timeLimit;
}
