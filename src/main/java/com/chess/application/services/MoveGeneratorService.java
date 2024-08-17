package com.chess.application.services;

import com.chess.application.controller.model.MoveWrapper;
import com.chess.application.model.Game;
import com.chess.application.model.NativePayload;
import com.chess.application.model.ReturnPayload;

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

    public void initialiseGame(Game game) {
        gameStoreService.createNewGame(game);
    }

    public ReturnPayload respondToMove(MoveWrapper move) {
        Game game = gameStoreService.getGame(move.getUuid());
        NativePayload nativePayload = NativePayload.builder()
                .fenString(game.getLastFenString())
                .settings(game.getSettings())
                .origin(move.getMove().getOrigin())
                .destination(move.getMove().getDestination())
                .promotion(move.getMove().getPromotion())
                .castle(move.getMove().isCastle())
                .castleType(move.getMove().getCastleType())
                .build();
        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);
        gameStoreService.updateGame(move.getUuid(), returnPayload);
        return returnPayload;
    }
}
