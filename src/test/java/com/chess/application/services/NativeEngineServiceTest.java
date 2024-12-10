package com.chess.application.services;

import com.chess.application.model.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


public class NativeEngineServiceTest {
    private final NativeEngineService nativeEngineService = new NativeEngineService();

    // engine repetition (client and engine caused)
    // client repetition
    // engine stalemate (client and engine caused)
    // client stalemate

    @CsvSource({"w", "b"})
    @ParameterizedTest
    public void testGetLegalMoves(String colour) {
        NativePayload nativePayload = NativePayload.builder()
            .fenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR " + colour + " KQkq - 0 1")
            .javaRequestType(JavaRequestType.LEGAL_MOVES)
            .origin(0)
            .destination(0)
            .promotion(0)
            .castle(false)
            .castleType(0)
            .hashValues(new long[0])
            .build();

        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);
        assertEquals(20, Arrays.stream(returnPayload.getClientLegalMoves()).filter(Objects::nonNull).count());
        assertEquals(20, returnPayload.getLegalLength());

        assertEquals(0L, returnPayload.getMoveHashClient());
        assertEquals(0L, returnPayload.getMoveHashEngine());
        assertNull(returnPayload.getFenStringClient());
        assertNull(returnPayload.getFenStringEngine());
    }

    @EnumSource(Colour.class)
    @ParameterizedTest
    public void testGetEngineMoves(Colour colour) {
        if (colour.equals(Colour.RANDOM)) {
            return;
        }
        NativePayload nativePayload = NativePayload.builder()
            .fenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
            .settings(Settings.builder().engineColour(colour.ordinal()).timeLimit(1).breadth(5).build())
            .javaRequestType(JavaRequestType.ENGINE)
            .origin(0)
            .destination(0)
            .promotion(0)
            .castle(false)
            .castleType(0)
            .hashValues(new long[0])
            .build();

        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);
        assertEquals(20, Arrays.stream(returnPayload.getClientLegalMoves()).filter(Objects::nonNull).count());
        assertEquals(20, returnPayload.getLegalLength());

        assertNotNull(returnPayload.getResponse());
        assertNotEquals(returnPayload.getResponse().getDestination(), returnPayload.getResponse().getOrigin());

        assertEquals(0L, returnPayload.getMoveHashClient());
        assertNotEquals(0L, returnPayload.getMoveHashEngine());
        assertTrue(returnPayload.getFenStringClient().isEmpty());
        assertNotNull(returnPayload.getFenStringEngine());
    }

    @EnumSource(Colour.class)
    @ParameterizedTest
    public void testGetEngineMoves_WithMove(Colour engineColour) {
        if (engineColour.equals(Colour.RANDOM)) {
            return;
        }
        NativePayload nativePayload = NativePayload.builder()
            .fenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
            .settings(Settings.builder().engineColour(engineColour.ordinal()).timeLimit(1).breadth(5).build())
            .javaRequestType(JavaRequestType.ENGINE)
            .origin(engineColour.equals(Colour.WHITE) ? Square.e7.ordinal() : Square.e2.ordinal())
            .destination(engineColour.equals(Colour.WHITE) ? Square.e5.ordinal() : Square.e4.ordinal())
            .promotion(0)
            .castle(false)
            .castleType(0)
            .hashValues(new long[0])
            .build();

        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);

        assertNotNull(returnPayload.getResponse());
        assertNotEquals(returnPayload.getResponse().getDestination(), returnPayload.getResponse().getOrigin());

        int legalLength = 30;
        if (Colour.BLACK.equals(engineColour) && (returnPayload.getResponse().getDestination() == Square.f5.ordinal() || returnPayload.getResponse().getDestination() == Square.d5.ordinal())) {
            legalLength = 31;
        }
        if (Colour.WHITE.equals(engineColour) && (returnPayload.getResponse().getDestination() == Square.f4.ordinal() || returnPayload.getResponse().getDestination() == Square.d4.ordinal())) {
            legalLength = 31;
        }

        assertEquals(legalLength, Arrays.stream(returnPayload.getClientLegalMoves()).filter(Objects::nonNull).count());
        assertEquals(legalLength, returnPayload.getLegalLength());

        assertNotEquals(0L, returnPayload.getMoveHashClient());
        assertNotEquals(0L, returnPayload.getMoveHashEngine());
        assertFalse(returnPayload.getFenStringClient().isEmpty());
        assertFalse(returnPayload.getFenStringEngine().isEmpty());
    }

    @CsvSource({"w", "b"})
    @ParameterizedTest
    public void testGetHumanMoves(String colour) {
        NativePayload nativePayload = NativePayload.builder()
            .fenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR " + colour + " KQkq - 0 1")
            .javaRequestType(JavaRequestType.HUMAN)
            .origin(0)
            .destination(0)
            .promotion(0)
            .castle(false)
            .castleType(0)
            .hashValues(new long[0])
            .build();

        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);
        assertEquals(20, Arrays.stream(returnPayload.getClientLegalMoves()).filter(Objects::nonNull).count());
        assertEquals(20, returnPayload.getLegalLength());

        assertNotNull(returnPayload.getResponse());
        assertEquals(returnPayload.getResponse().getDestination(), returnPayload.getResponse().getOrigin());

        assertEquals(0L, returnPayload.getMoveHashClient());
        assertEquals(0L, returnPayload.getMoveHashEngine());
        assertTrue(returnPayload.getFenStringClient().isEmpty());
        assertTrue(returnPayload.getFenStringEngine().isEmpty());
    }

    @CsvSource({"w", "b"})
    @ParameterizedTest
    public void testGetHumanMoves_WithMove(String colour) {
        NativePayload nativePayload = NativePayload.builder()
            .fenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR " + colour + " KQkq - 0 1")
            .javaRequestType(JavaRequestType.HUMAN)
            .origin(colour.equals("w") ? Square.e2.ordinal() : Square.e7.ordinal())
            .destination(colour.equals("w") ? Square.e4.ordinal() : Square.e5.ordinal())
            .promotion(0)
            .castle(false)
            .castleType(0)
            .hashValues(new long[0])
            .build();

        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);
        assertEquals(20, Arrays.stream(returnPayload.getClientLegalMoves()).filter(Objects::nonNull).count());
        assertEquals(20, returnPayload.getLegalLength());

        assertNotNull(returnPayload.getResponse());
        assertEquals(returnPayload.getResponse().getDestination(), returnPayload.getResponse().getOrigin());

        assertEquals(0L, returnPayload.getMoveHashClient());
        assertNotEquals(0L, returnPayload.getMoveHashEngine());
        assertFalse(returnPayload.getFenStringClient().isEmpty());
        assertTrue(returnPayload.getFenStringEngine().isEmpty());
    }

    @Test
    public void testClientVictoryByCheckmate() {
        Move move = new Move(Square.c7.ordinal(), Square.a7.ordinal(), 0, false, 0);
        Settings settings = Settings.builder()
            .breadth(5)
            .engineColour(Colour.WHITE.ordinal())
            .timeLimit(1)
            .build();

        NativePayload nativePayload = NativePayload.builder()
            .fenString("1r5k/2r5/8/8/8/8/8/K7 b - - 0 1")
            .origin(move.getOrigin())
            .destination(move.getDestination())
            .settings(settings)
            .build();

        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);

        assertEquals("1r5k/r7/8/8/8/8/8/K7 w - - ? ?", returnPayload.getFenStringClient());
        assertTrue(returnPayload.getFenStringEngine().isEmpty());
        assertEquals(Status.BLACK_VICTORY, returnPayload.getStatus());
        assertEquals(Status.Reason.CHECKMATE, returnPayload.getReason());
    }

    @Test
    public void testEngineVictoryByCheckmate() {
        NativePayload nativePayload = NativePayload.builder()
            .fenString("1r5k/2r5/8/8/8/8/8/K7 b - - 0 1")
            .settings(Settings.builder().engineColour(Colour.BLACK.ordinal()).timeLimit(1).breadth(5).build())
            .javaRequestType(JavaRequestType.ENGINE)
            .build();

        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);
        assertEquals("1r5k/r7/8/8/8/8/8/K7 w - - ? ?", returnPayload.getFenStringEngine());
        assertTrue(returnPayload.getFenStringClient().isEmpty());
        assertEquals(Status.BLACK_VICTORY, returnPayload.getStatus());
        assertEquals(Status.Reason.CHECKMATE, returnPayload.getReason());
    }

    @Test
    public void assertEngineFunctions() {
        NativeEngineService nativeEngineService = new NativeEngineService();

        // a move will be sent from the client
//        Move move = Move.builder().origin(Square.e2.ordinal()).destination(Square.e4.ordinal()).build();
        Move move = new Move(Square.e2.ordinal(), Square.e4.ordinal(), 0, false, 0);

        // transpositions will be saved in memory in Java
        // maybe not. Probably write and load them directly to / from disk
        Transposition transposition[] = new Transposition[1000000000];
        transposition[0] = Transposition.builder()
            .hashValue(54237895043278L)
            .build();

        Settings settings = Settings.builder()
            .breadth(5)
            .engineColour(1)
            .timeLimit(5)
            .build();

        // the move is added to the nativePayload with the current state of the game
        NativePayload nativePayload = NativePayload.builder()
            .fenString("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
            .origin(move.getOrigin())
            .destination(move.getDestination())
            .settings(settings)
            .build();

        // the nativePayload is sent to the C engine.
        // it must build the board from the fenString
        // and return the return payload: 
            // fenStrings: 1 representing the client's move, and 1 representing the engine's move
            // Also the engine's move in a simple format of origin, destination.
        ReturnPayload returnPayload = nativeEngineService.test_java_interface(nativePayload);
        int i = 0;
        while (returnPayload.getClientLegalMoves()[i] != null) {
            System.out.println("legal client response: " + returnPayload.getClientLegalMoves()[i].getMoveString());
            i += 1;
        }
        
    }
}

