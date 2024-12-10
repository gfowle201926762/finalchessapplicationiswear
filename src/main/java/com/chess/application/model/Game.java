package com.chess.application.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Data
@Table
public class Game {
    @Id
    String uuid;
    String whiteId;
    String blackId;
    OpponentType opponentType;
    @ElementCollection
    List<String> fenStrings;
    Status status;
    Status.Reason reason;
    @Embedded
    Settings settings;
    @ElementCollection
    List<Long> hashValues;
    @ElementCollection
    List<Move> moves;
    ZonedDateTime zonedDateTime;
    String dateTime;

    public static class GameBuilder {
        public GameBuilder zonedDateTime(ZonedDateTime stupid) {
            ZonedDateTime zonedDateTime = ZonedDateTime.now();
            this.zonedDateTime = zonedDateTime;
            this.dateTime = zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm dd-MM-YYYY"));
            System.out.println("dateTime: " + dateTime);
            return this;
        }
    }

    public String getFirstFenString() {
        if (fenStrings.isEmpty()) {
            return "";
        }
        return fenStrings.get(0);
    }

    public String getLastFenString() {
        return fenStrings.get(fenStrings.size() - 1);
    }

    public void addFenString(String fenString) {
        fenStrings.add(fenString);
    }

    public void addHashValue(long hashValue) {
        hashValues.add(hashValue);
    }

    public Game addMove(Move move) {
        if (this.moves == null) {
            this.moves = new ArrayList<>();
        }
        if (move.getOrigin() != 0L || move.getDestination() != 0L) {
            moves.add(move);
        }
        return this;
    }
}
