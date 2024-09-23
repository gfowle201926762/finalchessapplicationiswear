package com.chess.application.model;

public enum Status {
    ONGOING,
    WHITE_VICTORY,
    DRAW,
    BLACK_VICTORY;

    public static Status get(int index) {
        return values()[(int) index];
    }
}
