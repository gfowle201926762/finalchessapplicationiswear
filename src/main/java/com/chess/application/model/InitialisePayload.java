package com.chess.application.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InitialisePayload {
    String fenString;
    String id;
    String opponentUsername;
    Colour colour;
}
