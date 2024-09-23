package com.chess.application.controller.model;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder
public class MoveWrapper {
    String uuid;
    Move move;
}
