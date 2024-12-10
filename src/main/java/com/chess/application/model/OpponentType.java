package com.chess.application.model;

public enum OpponentType {
    HUMAN, COMPUTER;
    public static OpponentType get(int index) {
        return values()[(int) index];
    }
}
