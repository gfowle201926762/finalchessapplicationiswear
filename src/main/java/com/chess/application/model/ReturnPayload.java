package com.chess.application.model;


import com.chess.application.controller.model.Move;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ReturnPayload {
    String fenStringClient;
    String fenStringEngine;
    Move response;
    Move[] clientLegalMoves;
    long legalLength;
//    Status status;

    public ReturnPayload(String fenStringClient, String fenStringEngine, Move response, Move[] clientLegalMoves, long legalLength) {
        this.fenStringClient = fenStringClient;
        this.fenStringEngine = fenStringEngine;
        this.response = response;
        this.clientLegalMoves = clientLegalMoves;
        this.legalLength = legalLength;
//        this.status = Status.get((int) status);
    }
}
