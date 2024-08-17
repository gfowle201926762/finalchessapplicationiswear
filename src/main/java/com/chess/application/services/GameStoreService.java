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
    ConcurrentHashMap<UUID, Game> gameStore;
    NativeEngineService nativeEngineService;

    public GameStoreService(NativeEngineService nativeEngineService) {
        gameStore = new ConcurrentHashMap<>();
        this.nativeEngineService = nativeEngineService;
    }

    public void updateGame(UUID id, ReturnPayload returnPayload) {
        gameStore.get(id).addFenString(returnPayload.getFenStringClient());
        gameStore.get(id).addFenString(returnPayload.getFenStringEngine());
//        gameStore.get(id).setStatus(returnPayload.getStatus());
    }

    public void createNewGame(Game game) {
        // assuming playerId, opponentId, and Settings already sent in the payload.
        UUID id = UUID.randomUUID();
        List<String> fenList = new ArrayList<>();
        fenList.add("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        gameStore.put(game.getUuid(), game.toBuilder()
                .uuid(id)
                .fenStrings(fenList)
                .status(Status.ONGOING)
                .build());
    }

    public Game getGame(UUID id) {
        return gameStore.get(id);
    }
}
