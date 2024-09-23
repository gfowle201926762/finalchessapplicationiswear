package com.chess.application.services;

import com.chess.application.model.Game;
import com.chess.application.model.ReturnPayload;
import com.chess.application.model.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class GameStoreService {

    // this will need concurrency control.
    ConcurrentHashMap<String, Game> gameStore;

    public GameStoreService() {
        gameStore = new ConcurrentHashMap<>();
    }

    public void updateGame(String id, Status status) {
        gameStore.put(id, gameStore.get(id).toBuilder().status(status).build());
    }

    public void updateGame(String id, ReturnPayload returnPayload) {
        if (returnPayload.getFenStringClient() != null && !returnPayload.getFenStringClient().isEmpty()) {
            gameStore.get(id).addFenString(returnPayload.getFenStringClient());
        }
        if (returnPayload.getFenStringEngine() != null && !returnPayload.getFenStringEngine().isEmpty()) {
            gameStore.get(id).addFenString(returnPayload.getFenStringEngine());
        }
        if (returnPayload.getMoveHashClient() != 0) {
            gameStore.get(id).addHashValue(returnPayload.getMoveHashClient());
        }
        if (returnPayload.getMoveHashEngine() != 0) {
            gameStore.get(id).addHashValue(returnPayload.getMoveHashEngine());
        }

        System.out.println("updated game: " + gameStore.get(id).getFenStrings());
    }

    public Game createNewGame(Game game) {
        // assuming playerId, opponentId, and Settings already sent in the payload.
        String id = UUID.randomUUID().toString();
        List<Long> hashValues = new ArrayList<>();
        List<String> fenList = new ArrayList<>();
        fenList.add("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
//        fenList.add("1r5k/2r5/8/8/8/8/8/K7 b - - 0 1");
        Game initialisedGame = game.toBuilder()
            .uuid(id)
            .fenStrings(fenList)
            .status(Status.ONGOING)
            .hashValues(hashValues)
            .build();
        gameStore.put(id, initialisedGame);
//        System.out.println("id: " + id + ", createNewGame: " + gameStore.get(game.getUuid()));
        return initialisedGame;
    }

    public Game getGame(String id) {
        return gameStore.get(id);
    }
}
