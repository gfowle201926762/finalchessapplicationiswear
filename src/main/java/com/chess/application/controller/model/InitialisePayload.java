package com.chess.application.controller.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class InitialisePayload {
    String fenString;
    String id;
}
