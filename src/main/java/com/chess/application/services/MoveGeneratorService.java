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

//    private final GameStoreService gameStoreService;
    private final GameService gameService;
    private final NativeEngineService nativeEngineService;

    @Autowired
    public MoveGeneratorService(GameService gameService, NativeEngineService nativeEngineService) {
        this.gameService = gameService;
        this.nativeEngineService = nativeEngineService;
    }

    public Game initialiseGame(Game game) {
        return gameService.createNewGame(game);
    }

    public void updateGameSettings(String id, Status status, Status.Reason reason) {
        gameService.updateGame(id, status, reason);
    }

    public ReturnPayload respondToMove(MoveWrapper move) {
        gameService.updateGame(move.getUuid(), move.getMove());
        Game game = gameService.getGame(move.getUuid());
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
        System.out.println("java request type: " + nativePayload.getJavaRequestType() + ", engineColour: " + nativePayload.getSettings().getEngineColour());
        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);
        System.out.println("client fen: " + returnPayload.getFenStringClient() + ";           engine fen: " + returnPayload.getFenStringEngine());

        gameService.updateGame(move.getUuid(), returnPayload);
        return returnPayload;
    }
}
