package com.chess.application.controller.model;

import com.chess.application.model.JavaRequestType;
import lombok.Builder;
import lombok.Value;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class MoveWrapper {
    String uuid;
    Move move;
    JavaRequestType javaRequestType;
}
