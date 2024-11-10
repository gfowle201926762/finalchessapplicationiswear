package com.chess.application.controller.model;

import com.chess.application.model.Colour;
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
