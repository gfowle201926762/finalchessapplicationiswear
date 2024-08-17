package com.chess.application.controller.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Scores {
    Moves moves;
    int eval;
}
