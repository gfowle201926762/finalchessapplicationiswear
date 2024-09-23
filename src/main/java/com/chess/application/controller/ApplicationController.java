package com.chess.application.controller;

import com.chess.application.controller.model.MoveWrapper;
import com.chess.application.model.Game;
import com.chess.application.model.ReturnPayload;
import com.chess.application.services.MoveGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@RestController
public class ApplicationController {

    private final MoveGeneratorService moveGeneratorService;
    private final ResourceLoader resourceLoader;
    private final TemplateEngine templateEngine;

    @Autowired
    public ApplicationController(MoveGeneratorService moveGeneratorService, ResourceLoader resourceLoader, TemplateEngine templateEngine) {
        this.moveGeneratorService = moveGeneratorService;
        this.resourceLoader = resourceLoader;
        this.templateEngine = templateEngine;
    }

    @GetMapping("/")
    public ResponseEntity<String> index(String id) {
        Context context = new Context();
        context.setVariable("gameId", id);
        String htmlContent = templateEngine.process("index", context);
        return ResponseEntity
                .status(200)
                .header(HttpHeaders.CONTENT_TYPE, "text/html")
                .body(htmlContent);
    }

    @MessageMapping("/move")
    @SendTo("/topic/move")
    public String sendMove(@Payload String received) {
        // move is received from the client.
        // What is returned here is sent back to the client.
//        return moveGeneratorService.respondToMove(clientMove);
        System.out.println("IN SEND MOVE JAVA");
        return "message received.";
    }

    @MessageMapping("/start_game")
    @SendTo("/topic/move")
    public ReturnPayload startGame(@Payload Game game) {
        // move is received from the client.
        // What is returned here is sent back to the client.
        return null;
//        return moveGeneratorService.initialiseGame(game);
    }
}
