package com.chess.application.model;

public enum Status {
    ONGOING,
    WHITE_VICTORY,
    DRAW,
    BLACK_VICTORY;

    public static Status get(int index) {
        return values()[(int) index];
    }

    public enum Reason {
        NONE,
        CHECKMATE,
        ABANDONMENT,
        RESIGNATION,
        STALEMATE,
        REPETITION,
        INSUFFICIENT_MATERIAL;

        public static Reason get(int index) {
            return values()[(int) index];
        }
    }
}
