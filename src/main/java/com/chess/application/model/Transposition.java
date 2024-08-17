package com.chess.application.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Transposition {
    long hashValue;
    int eval;
    int depth;
    int flag;
}
