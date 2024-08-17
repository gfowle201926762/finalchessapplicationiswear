package com.chess.application.model;

import com.chess.application.controller.model.Settings;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.Value;
import java.util.List;

import java.util.UUID;

@Data
@Builder(toBuilder = true)
public class Game {
    UUID uuid;
    UUID playerId;
    UUID opponentId;
    List<String> fenStrings;
    Status status;
    Settings settings;

    public String getLastFenString() {
        return fenStrings.get(fenStrings.size() - 1);
    }

    public void addFenString(String fenString) {
        fenStrings.add(fenString);
    }

}
