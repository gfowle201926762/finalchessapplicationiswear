package com.chess.application.controller;

import com.chess.application.model.CustomUserDetails;
import com.chess.application.model.User;
import com.chess.application.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/getUsername")
    public ResponseEntity<String> checkAuthentication(@AuthenticationPrincipal CustomUserDetails customUserDetails, HttpSession session) {
        String clientId;
        if (customUserDetails != null) {
            User user = userService.findByUsername(customUserDetails.getUsername());
            clientId = user.getUsername();
        } else {
            clientId = "guest-" + UUID.randomUUID().toString();
        }
        session.setAttribute("clientId", clientId);
        return ResponseEntity.ok().body(clientId);
    }
}
