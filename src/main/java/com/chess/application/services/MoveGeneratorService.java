package com.chess.application.services;

import com.chess.application.controller.model.MoveWrapper;
import com.chess.application.model.Game;
import com.chess.application.model.NativePayload;
import com.chess.application.model.ReturnPayload;

import com.chess.application.model.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MoveGeneratorService {

    private final GameStoreService gameStoreService;
    private final NativeEngineService nativeEngineService;

    @Autowired
    public MoveGeneratorService(GameStoreService gameStoreService, NativeEngineService nativeEngineService) {
        this.gameStoreService = gameStoreService;
        this.nativeEngineService = nativeEngineService;
    }

    public Game initialiseGame(Game game) {
        return gameStoreService.createNewGame(game);
    }

    public void updateGameSettings(String id, Status status) {
        gameStoreService.updateGame(id, status);
    }

    public ReturnPayload respondToMove(MoveWrapper move) {
        Game game = gameStoreService.getGame(move.getUuid());
        NativePayload nativePayload = NativePayload.builder()
            .fenString(game.getLastFenString())
            .settings(game.getSettings())
            .javaRequestType(move.getJavaRequestType())
            .origin(move.getMove().getOrigin())
            .destination(move.getMove().getDestination())
            .promotion(move.getMove().getPromotion())
            .castle(move.getMove().isCastle())
            .castleType(move.getMove().getCastleType())
            .hashValues(game.getHashValues() == null ? null : game.getHashValues().stream().mapToLong(Long::longValue).toArray())
            .build();
        System.out.println("java request type: " + nativePayload.getJavaRequestType() + ", startPlayer: " + nativePayload.getSettings().getStartPlayer());
        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);
        System.out.println("client fen: " + returnPayload.getFenStringClient() + ";           engine fen: " + returnPayload.getFenStringEngine());

        gameStoreService.updateGame(move.getUuid(), returnPayload);
        return returnPayload;
    }
}
