package com.chess.application.model;

import jakarta.persistence.Embeddable;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Embeddable
public class Move {
    long origin;
    long destination;
    long promotion;
    boolean castle;
    long castleType;
    long eval;

//    public Move(long origin, long destination, long promotion, boolean castle, long castleType, long eval) {
//        this.origin = origin;
//        this.destination = destination;
//        this.promotion = promotion;
//        this.castle = castle;
//        this.castleType = castleType;
//        this.eval = eval;
//    }

    public Move(long origin, long destination, long promotion, boolean castle, long castleType) {
        this.origin = origin;
        this.destination = destination;
        this.promotion = promotion;
        this.castle = castle;
        this.castleType = castleType;
    }

    public String getMoveString() {
        return "origin: " + Square.get(origin) + ", destination: " + Square.get(destination) + ", promotion: " + promotion + ", castle: " + castle + ", castleType: " + castleType;
    }
}
