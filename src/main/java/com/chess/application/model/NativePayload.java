package com.chess.application.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder()
public class NativePayload {
    long[] hashValues;
    String fenString;
    Settings settings;
    long javaRequestType;
    long clientColour;

    long origin;
    long destination;
    long promotion;
    boolean castle;
    long castleType;

    public static class NativePayloadBuilder {
        public NativePayloadBuilder javaRequestType(JavaRequestType javaRequestType) {
            this.javaRequestType = javaRequestType.ordinal();
            return this;
        }
    }
}
