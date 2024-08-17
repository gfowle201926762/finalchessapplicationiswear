package com.chess.application.controller;

import com.chess.application.Application;
import com.chess.application.controller.model.Move;
import com.chess.application.controller.model.MoveWrapper;
import com.chess.application.controller.model.Settings;
import com.chess.application.model.Game;
import com.chess.application.model.NativePayload;
import com.chess.application.model.ReturnPayload;
import com.chess.application.services.MoveGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class ApplicationController {

    private final MoveGeneratorService moveGeneratorService;

    @Autowired
    public ApplicationController(MoveGeneratorService moveGeneratorService) {
        this.moveGeneratorService = moveGeneratorService;
    }

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @MessageMapping("/send_move")
    @SendTo("/topic/move")
    public ReturnPayload sendMove(@Payload MoveWrapper clientMove) {
        // move is received from the client.
        // What is returned here is sent back to the client.
        return moveGeneratorService.respondToMove(clientMove);
    }

    @MessageMapping("/start_game")
    @SendTo("/topic/move")
    public ReturnPayload startGame(@Payload Game game) {
        // move is received from the client.
        // What is returned here is sent back to the client.
        return ReturnPayload.builder().build();
//        return moveGeneratorService.initialiseGame(game);
    }
}
