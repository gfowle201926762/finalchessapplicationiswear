package com.chess.application.controller.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Settings {
    long breadth;
    long startPlayer;
    long timeLimit;
}
