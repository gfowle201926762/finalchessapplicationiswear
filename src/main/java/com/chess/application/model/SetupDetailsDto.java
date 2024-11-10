package com.chess.application.model;

import com.chess.application.controller.model.OpponentType;
import lombok.Data;

@Data
public class SetupDetailsDto {
    OpponentType opponentType;
    Colour colour;
    int timeLimit;
    int breadth;
}
