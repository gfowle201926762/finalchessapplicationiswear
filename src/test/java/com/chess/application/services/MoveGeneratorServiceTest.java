package com.chess.application.services;

import com.chess.application.controller.model.*;
import com.chess.application.model.*;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MoveGeneratorServiceTest {

    private GameStoreService gameStoreService;
    private NativeEngineService nativeEngineService;
    private MoveGeneratorService moveGeneratorService;

    @BeforeEach
    public void setup() {
        gameStoreService = new GameStoreService();
        nativeEngineService = new NativeEngineService();
        moveGeneratorService = new MoveGeneratorService(gameStoreService, nativeEngineService);
    }

    @Test
    public void testBug3() {
        Move move = new Move(Square.c7.ordinal(), Square.b7.ordinal(), 0, false, 0);

        Settings settings = Settings.builder()
            .breadth(5)
            .startPlayer(Colour.WHITE.ordinal())
            .timeLimit(1)
            .build();

        Game game = Game.builder()
            .opponentType(OpponentType.COMPUTER)
            .settings(settings)
            .hashValues(new ArrayList<>(List.of(0L)))
            .build();

        Game initialisedGame = gameStoreService.createNewGame(game);
        gameStoreService.updateGame(initialisedGame.getUuid(), new ReturnPayload(
            "5rk1/2qb1p1p/3bp3/1n1p1p2/2pP3P/2P1P3/1PN2PP1/R3KBNR b KQ - ? ?",
            null,
            null,
            null,
            0,
            0L,
            0L,
            0L
        ));

        MoveWrapper moveWrapper = MoveWrapper.builder()
            .uuid(initialisedGame.getUuid())
            .move(move)
            .build();

        ReturnPayload returnPayload = moveGeneratorService.respondToMove(moveWrapper);
        System.out.println(returnPayload.getFenStringClient());
        System.out.println(returnPayload.getFenStringEngine());
        assertEquals(Status.ONGOING, returnPayload.getStatus());
    }

    @Test
    public void testBug2() {
        // NOT RESOLVED
        Move move = new Move(Square.g3.ordinal(), Square.f4.ordinal(), 0, false, 0);

        Settings settings = Settings.builder()
            .breadth(5)
            .startPlayer(Colour.WHITE.ordinal())
            .timeLimit(1)
            .build();

        Game game = Game.builder()
            .opponentType(OpponentType.COMPUTER)
            .settings(settings)
            .hashValues(new ArrayList<>(List.of(0L)))
            .build();

        // 7Q/8/7p/8/1K6/1P4k1/8/8 b - - ? ?   ENGINE
        // 7Q/8/8/7p/1K6/1P4k1/8/8 w - - ? ?  CLIENT
        // 8/8/8/7Q/1K6/1P4k1/8/8 b - - ? ?   ENGINE
        // 8/8/8/7Q/1K3k2/1P6/8/8 w - - ? ?   CLIENT

        Game initialisedGame = gameStoreService.createNewGame(game);
        gameStoreService.updateGame(initialisedGame.getUuid(), new ReturnPayload(
            "8/8/8/7Q/1K6/1P4k1/8/8 b - - ? ?",
            null,
            null,
            null,
            0,
            0L,
            0L,
            0L
        ));

        MoveWrapper moveWrapper = MoveWrapper.builder()
            .uuid(initialisedGame.getUuid())
            .move(move)
            .build();

        ReturnPayload returnPayload = moveGeneratorService.respondToMove(moveWrapper);
        System.out.println(returnPayload.getFenStringClient());
        System.out.println(returnPayload.getFenStringEngine());
        assertEquals(Status.ONGOING, returnPayload.getStatus());
    }

    @Test
    public void testBug1() {
        Move move = new Move(Square.f5.ordinal(), Square.e4.ordinal(), 0, false, 0);

        Settings settings = Settings.builder()
            .breadth(5)
            .startPlayer(Colour.WHITE.ordinal())
            .timeLimit(1)
            .build();

        Game game = Game.builder()
            .opponentType(OpponentType.COMPUTER)
            .settings(settings)
            .hashValues(new ArrayList<>(List.of(0L)))
            .build();

        // b2r4/8/p7/5k2/PPN2pp1/2Pp4/5K2/3R2N1 b - - ? ?   ENGINE
        // b2r4/8/p7/5k2/PPN2p2/2Pp2p1/5K2/3R2N1 w - - ? ?  CLIENT
        // b2r4/8/p7/5k2/PPN2p2/2Pp2p1/8/3RK1N1 b - - ? ?   ENGINE
        // b2r4/8/p7/8/PPN1kp2/2Pp2p1/8/3RK1N1 w - - ? ?    CLIENT

        Game initialisedGame = gameStoreService.createNewGame(game);
        gameStoreService.updateGame(initialisedGame.getUuid(), new ReturnPayload(
            "b2r4/8/p7/5k2/PPN2p2/2Pp2p1/8/3RK1N1 b - - ? ?",
            null,
            null,
            null,
            0,
            0L,
            0L,
            0L
        ));

        MoveWrapper moveWrapper = MoveWrapper.builder()
            .uuid(initialisedGame.getUuid())
            .move(move)
            .build();

        ReturnPayload returnPayload = moveGeneratorService.respondToMove(moveWrapper);
        System.out.println(returnPayload.getFenStringClient());
        System.out.println(returnPayload.getFenStringEngine());
        assertEquals(Status.ONGOING, returnPayload.getStatus());
    }

    @Test
    public void canHandleDrawByRepetition() {

        Move move = new Move(Square.g3.ordinal(), Square.g1.ordinal(), 0, false, 0);

        Settings settings = Settings.builder()
            .breadth(5)
            .startPlayer(Colour.WHITE.ordinal())
            .timeLimit(1)
            .build();

        Game game = Game.builder()
            .opponentType(OpponentType.COMPUTER)
            .settings(settings)
            .hashValues(new ArrayList<>(List.of(0L)))
            .build();

        Game initialisedGame = gameStoreService.createNewGame(game);
        gameStoreService.updateGame(initialisedGame.getUuid(), new ReturnPayload(
            "1r5k/8/8/8/8/6qr/8/K7 b - - 0 1",
            null,
            null,
            null,
            0,
            0L,
            0L,
            0L
        ));

        MoveWrapper moveWrapper = MoveWrapper.builder()
            .uuid(initialisedGame.getUuid())
            .move(move)
            .build();

        ReturnPayload returnPayload = moveGeneratorService.respondToMove(moveWrapper);
        assertEquals(Status.ONGOING, returnPayload.getStatus());
        assertEquals("1r5k/8/8/8/8/7r/8/K5q1 w - - ? ?", returnPayload.getFenStringClient());
        assertEquals("1r5k/8/8/8/8/7r/K7/6q1 b - - ? ?", returnPayload.getFenStringEngine());

        // NEXT MOVE
        Move move2 = new Move(Square.g1.ordinal(), Square.g2.ordinal(), 0, false, 0);
        MoveWrapper moveWrapper2 = MoveWrapper.builder()
            .uuid(initialisedGame.getUuid())
            .move(move2)
            .build();
        ReturnPayload returnPayload2 = moveGeneratorService.respondToMove(moveWrapper2);
        assertEquals(Status.ONGOING, returnPayload2.getStatus());
        assertEquals("1r5k/8/8/8/8/7r/K5q1/8 w - - ? ?", returnPayload2.getFenStringClient());
        assertEquals("1r5k/8/8/8/8/7r/6q1/K7 b - - ? ?", returnPayload2.getFenStringEngine());

        // NEXT MOVE
        Move move3 = new Move(Square.g2.ordinal(), Square.g1.ordinal(), 0, false, 0);
        MoveWrapper moveWrapper3 = MoveWrapper.builder()
            .uuid(initialisedGame.getUuid())
            .move(move3)
            .build();
        ReturnPayload returnPayload3 = moveGeneratorService.respondToMove(moveWrapper3);
        assertEquals(Status.ONGOING, returnPayload3.getStatus());
        assertEquals("1r5k/8/8/8/8/7r/8/K5q1 w - - ? ?", returnPayload3.getFenStringClient());
        assertEquals("1r5k/8/8/8/8/7r/K7/6q1 b - - ? ?", returnPayload3.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload4 = moveGeneratorService.respondToMove(moveWrapper2);
        assertEquals(Status.ONGOING, returnPayload4.getStatus());
        assertEquals("1r5k/8/8/8/8/7r/K5q1/8 w - - ? ?", returnPayload4.getFenStringClient());
        assertEquals("1r5k/8/8/8/8/7r/6q1/K7 b - - ? ?", returnPayload4.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload5 = moveGeneratorService.respondToMove(moveWrapper3);
        assertEquals(Status.DRAW, returnPayload5.getStatus());
        assertEquals("1r5k/8/8/8/8/7r/8/K5q1 w - - ? ?", returnPayload5.getFenStringClient());
        assertTrue(returnPayload5.getFenStringEngine().isEmpty());
    }
}