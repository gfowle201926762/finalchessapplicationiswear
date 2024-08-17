package com.chess.application.services;

import com.chess.application.controller.model.*;
import com.chess.application.model.*;

import org.junit.jupiter.api.Test;


public class NativeEngineServiceTest {

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
            .startPlayer(1)
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

        System.out.println("FEN Client: " + returnPayload.getFenStringClient());
        System.out.println("FEN Engine: " + returnPayload.getFenStringEngine());
        System.out.println("engine move: " + returnPayload.getResponse().getMoveString());

        int i = 0;
        while (returnPayload.getClientLegalMoves()[i] != null) {
            System.out.println("legal client response: " + returnPayload.getClientLegalMoves()[i].getMoveString());
            i += 1;
        }
        
    }
}

