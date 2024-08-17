package com.chess.application.controller.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Moves {
    Move[] moves;
    int length;
    boolean prune_flag;
}
