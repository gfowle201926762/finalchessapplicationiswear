package com.chess.application.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class MoveWrapper {
    String uuid;
    Move move;
    JavaRequestType javaRequestType;
}
