package com.chess.application.model;


import com.chess.application.controller.model.Move;
import lombok.Builder;
import lombok.Getter;

import java.io.Serializable;

//@Builder
@Getter
public class ReturnPayload implements Serializable {
    String fenStringClient;
    String fenStringEngine;
    Move response;
    Move[] clientLegalMoves;
    long legalLength;
    Status status;
    long moveHashClient;
    long moveHashEngine;

    public ReturnPayload(String fenStringClient, String fenStringEngine, Move response, Move[] clientLegalMoves, long legalLength, long statusLong, long moveHashClient, long moveHashEngine) {
        this.fenStringClient = fenStringClient;
        this.fenStringEngine = fenStringEngine;
        this.response = response;
        this.clientLegalMoves = clientLegalMoves;
        this.legalLength = legalLength;
        this.status = Status.get((int) statusLong);
        this.moveHashClient = moveHashClient;
        this.moveHashEngine = moveHashEngine;
    }
}
