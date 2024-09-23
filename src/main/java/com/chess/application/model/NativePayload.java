package com.chess.application.model;

import com.chess.application.controller.model.*;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class NativePayload {
    long[] hashValues;
    String fenString;
    Settings settings;

    long origin;
    long destination;
    long promotion;
    boolean castle;
    long castleType;
}
