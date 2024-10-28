package com.chess.application.model;

public enum Colour {
    WHITE, BLACK, RANDOM;

    public static Colour get(int index) {
        return values()[(int) index];
    }
}
