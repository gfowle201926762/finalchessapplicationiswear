package com.chess.application.model;

public enum Status {
    ONGOING,
    WHITE,
    DRAW,
    BLACK;

    public static Status get(long index) {
        return values()[(int) index];
    }
}
