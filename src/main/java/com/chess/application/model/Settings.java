package com.chess.application.model;

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
