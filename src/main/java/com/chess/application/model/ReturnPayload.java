package com.chess.application.model;


import lombok.*;

import java.io.Serializable;

//@Builder(toBuilder = true)
@Getter
@Setter
public class ReturnPayload implements Serializable {
    String fenStringClient;
    String fenStringEngine;
    Move response;
    Move[] clientLegalMoves;
    long legalLength;
    Status status;
    Status.Reason reason;
    long moveHashClient;
    long moveHashEngine;

//    public static class ReturnPayloadBuilder {
//        public ReturnPayloadBuilder javaRequestType(long statusLong) {
//            this.status = Status.get((int) statusLong);
//            return this;
//        }
//    }

    public ReturnPayload(String fenStringClient, String fenStringEngine, Move response, Move[] clientLegalMoves, long legalLength, long statusLong, long reasonLong, long moveHashClient, long moveHashEngine) {
        this.fenStringClient = fenStringClient;
        this.fenStringEngine = fenStringEngine;
        this.response = response;
        this.clientLegalMoves = clientLegalMoves;
        this.legalLength = legalLength;
        this.status = Status.get((int) statusLong);
        this.reason = Status.Reason.get((int) reasonLong);
        this.moveHashClient = moveHashClient;
        this.moveHashEngine = moveHashEngine;
    }

    public ReturnPayload(Status status, Status.Reason reason) {
        this.status = status;
        this.reason = reason;
    }
}
