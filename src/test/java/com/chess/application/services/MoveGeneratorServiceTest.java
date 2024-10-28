package com.chess.application.services;

import com.chess.application.controller.model.*;
import com.chess.application.model.*;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
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
            .javaRequestType(JavaRequestType.ENGINE)
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
            .javaRequestType(JavaRequestType.ENGINE)
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
            .javaRequestType(JavaRequestType.ENGINE)
            .move(move)
            .build();

        ReturnPayload returnPayload = moveGeneratorService.respondToMove(moveWrapper);
        System.out.println(returnPayload.getFenStringClient());
        System.out.println(returnPayload.getFenStringEngine());
        assertEquals(Status.ONGOING, returnPayload.getStatus());
    }


    @ParameterizedTest
    @EnumSource(JavaRequestType.class)
    public void canHandleDrawByRepetition(JavaRequestType javaRequestType) {
        if (javaRequestType.equals(JavaRequestType.LEGAL_MOVES)) {
            return;
        }

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
            .javaRequestType(JavaRequestType.ENGINE)
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
            .javaRequestType(JavaRequestType.ENGINE)
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
            .javaRequestType(JavaRequestType.ENGINE)
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

    @Test
    public void canHandleDrawByRepetitionHuman() {

        Move move = new Move(Square.d2.ordinal(), Square.d4.ordinal(), 0, false, 0);
        Move move2 = new Move(Square.e7.ordinal(), Square.e5.ordinal(), 0, false, 0);
        Move move3 = new Move(Square.d4.ordinal(), Square.e5.ordinal(), 0, false, 0); // first
        Move move4 = new Move(Square.d8.ordinal(), Square.e7.ordinal(), 0, false, 0);
        Move move5 = new Move(Square.d1.ordinal(), Square.d2.ordinal(), 0, false, 0);
        Move move6 = new Move(Square.e7.ordinal(), Square.d8.ordinal(), 0, false, 0);
        Move move7 = new Move(Square.d2.ordinal(), Square.d1.ordinal(), 0, false, 0); // second


        Settings settings = Settings.builder()
            .startPlayer(Colour.WHITE.ordinal())
            .build();

        Game game = Game.builder()
            .opponentType(OpponentType.HUMAN)
            .settings(settings)
            .hashValues(new ArrayList<>(List.of(0L)))
            .build();

        Game initialisedGame = gameStoreService.createNewGame(game);

        MoveWrapper moveWrapper = MoveWrapper.builder()
            .uuid(initialisedGame.getUuid())
            .move(move)
            .javaRequestType(JavaRequestType.HUMAN)
            .build();

        ReturnPayload returnPayload = moveGeneratorService.respondToMove(moveWrapper);
        assertEquals(Status.ONGOING, returnPayload.getStatus());
        assertEquals("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 ? ?", returnPayload.getFenStringClient());
        assertEquals("", returnPayload.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload2 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move2).build());
        assertEquals(Status.ONGOING, returnPayload2.getStatus());
        assertEquals("rnbqkbnr/pppp1ppp/8/4p3/3P4/8/PPP1PPPP/RNBQKBNR w KQkq e6 ? ?", returnPayload2.getFenStringClient());
        assertEquals("", returnPayload2.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload3 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move3).build());
        assertEquals(Status.ONGOING, returnPayload3.getStatus());
        assertEquals("rnbqkbnr/pppp1ppp/8/4P3/8/8/PPP1PPPP/RNBQKBNR b KQkq - ? ?", returnPayload3.getFenStringClient());
        assertEquals("", returnPayload3.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload4 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move4).build());
        assertEquals(Status.ONGOING, returnPayload4.getStatus());
        assertEquals("rnb1kbnr/ppppqppp/8/4P3/8/8/PPP1PPPP/RNBQKBNR w KQkq - ? ?", returnPayload4.getFenStringClient());
        assertEquals("", returnPayload4.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload5 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move5).build());
        assertEquals(Status.ONGOING, returnPayload5.getStatus());
        assertEquals("rnb1kbnr/ppppqppp/8/4P3/8/8/PPPQPPPP/RNB1KBNR b KQkq - ? ?", returnPayload5.getFenStringClient());
        assertEquals("", returnPayload5.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload6 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move6).build());
        assertEquals(Status.ONGOING, returnPayload6.getStatus());
        assertEquals("rnbqkbnr/pppp1ppp/8/4P3/8/8/PPPQPPPP/RNB1KBNR w KQkq - ? ?", returnPayload6.getFenStringClient());
        assertEquals("", returnPayload6.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload7 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move7).build());
        assertEquals(Status.ONGOING, returnPayload7.getStatus()); // OH, DEAR!
        assertEquals("rnbqkbnr/pppp1ppp/8/4P3/8/8/PPP1PPPP/RNBQKBNR b KQkq - ? ?", returnPayload7.getFenStringClient());
        assertEquals("", returnPayload7.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload8 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move4).build());
        assertEquals(Status.ONGOING, returnPayload8.getStatus());
        assertEquals("rnb1kbnr/ppppqppp/8/4P3/8/8/PPP1PPPP/RNBQKBNR w KQkq - ? ?", returnPayload8.getFenStringClient());
        assertEquals("", returnPayload8.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload9 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move5).build());
        assertEquals(Status.ONGOING, returnPayload9.getStatus());
        assertEquals("rnb1kbnr/ppppqppp/8/4P3/8/8/PPPQPPPP/RNB1KBNR b KQkq - ? ?", returnPayload9.getFenStringClient());
        assertEquals("", returnPayload9.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload10 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move6).build());
        assertEquals(Status.ONGOING, returnPayload10.getStatus());
        assertEquals("rnbqkbnr/pppp1ppp/8/4P3/8/8/PPPQPPPP/RNB1KBNR w KQkq - ? ?", returnPayload10.getFenStringClient());
        assertEquals("", returnPayload10.getFenStringEngine());

        // NEXT MOVE
        ReturnPayload returnPayload11 = moveGeneratorService.respondToMove(moveWrapper.toBuilder().move(move7).build());
        assertEquals(Status.DRAW, returnPayload11.getStatus()); // OH, DEAR!
        assertEquals("rnbqkbnr/pppp1ppp/8/4P3/8/8/PPP1PPPP/RNBQKBNR b KQkq - ? ?", returnPayload11.getFenStringClient());
        assertEquals("", returnPayload11.getFenStringEngine());
    }
}