package com.chess.application.services;

import com.chess.application.controller.model.Move;
import com.chess.application.model.Game;
import com.chess.application.model.ReturnPayload;
import com.chess.application.model.Status;

import java.util.List;

public interface GameService {
    Game getGame(String id);
    void updateGame(String id, Status status, Status.Reason reason);
    void updateGame(String id, Move move);
    void updateGame(String id, ReturnPayload returnPayload);
    Game createNewGame(Game game);
    List<Game> getGamesForPlayer(String id);
}
