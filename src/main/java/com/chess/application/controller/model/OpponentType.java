package com.chess.application.controller.model;

import com.chess.application.model.Colour;

public enum OpponentType {
    HUMAN, COMPUTER;
    public static OpponentType get(int index) {
        return values()[(int) index];
    }
}
