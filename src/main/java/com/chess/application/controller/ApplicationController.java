package com.chess.application.controller;

import com.chess.application.controller.model.Settings;
import com.chess.application.model.*;
import com.chess.application.services.GameService;
import com.chess.application.services.MoveGeneratorService;
import com.chess.application.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.Principal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Controller
public class ApplicationController {

    private final MoveGeneratorService moveGeneratorService;
    private final ResourceLoader resourceLoader;
    private final TemplateEngine templateEngine;
    private final UserService userService;
    private final GameService gameService;

    @Autowired
    public ApplicationController(MoveGeneratorService moveGeneratorService, ResourceLoader resourceLoader, TemplateEngine templateEngine, UserService userService, GameService gameService) {
        this.moveGeneratorService = moveGeneratorService;
        this.resourceLoader = resourceLoader;
        this.templateEngine = templateEngine;
        this.userService = userService;
        this.gameService = gameService;
//        this.gameStoreService = gameStoreService;
    }

    private boolean checkInvalidSession(CustomUserDetails customUserDetails) {
        if (customUserDetails != null && userService.findByUsername(customUserDetails.getUsername()) == null) {
            System.out.println("INVALID SESSION!");
            return true;
        }
        return false;
    }

    @GetMapping("/")
    public String landing(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails, Principal principal, HttpSession session) {
        if (checkInvalidSession(customUserDetails)) {
            return "redirect:/logout";
        }
        System.out.println(principal);
        if (customUserDetails != null) {
            model.addAttribute("user", userService.findByUsername(customUserDetails.getUsername()));
        }
        model.addAttribute("authenticated", customUserDetails != null);
        System.out.println("user: " + model.getAttribute("user"));
        System.out.println("authenticated: " + model.getAttribute("authenticated"));
        System.out.println("customUserDetails: " + customUserDetails);
        return "landing";
    }

    @GetMapping("/start-game")
    public String startGame(Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails, HttpSession session) {
        if (checkInvalidSession(customUserDetails)) {
            return "redirect:/logout";
        }
        if (customUserDetails != null) {
            User user = userService.findByUsername(customUserDetails.getUsername());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("user", user);
        }
        model.addAttribute("authenticated", customUserDetails != null);
        return "startGame";
    }

    @GetMapping("/game/{gameId}")
    public String game(@PathVariable String gameId, Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails, HttpSession session) {
        if (checkInvalidSession(customUserDetails)) {
            return "redirect:/logout";
        }
        if (customUserDetails != null) {
            model.addAttribute("user", userService.findByUsername(customUserDetails.getUsername()));
        }
        model.addAttribute("authenticated", customUserDetails != null);
        model.addAttribute("username", session.getAttribute("clientId"));
        Game game = gameService.getGame(gameId);
        if (game == null) {
            return "index";
        }
        model.addAttribute("game", game);

        return "index";
    }

    @GetMapping("/login")
    public String login(Model model, UserDto userDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        model.addAttribute("userDto", userDto);
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model, UserDto userDto, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        model.addAttribute("userDto", userDto);
        return "register";
//        Context context = new Context();
//        String htmlContent = templateEngine.process("register", context);
//        return ResponseEntity
//            .status(200)
//            .header(HttpHeaders.CONTENT_TYPE, "text/html")
//            .body(htmlContent);
    }
    // might not need context -> if you return a view name as a String, apparently Spring automatically makes the Model available to the thymeleaf context. Only if you use @Controller instead of @RestController

    @PostMapping("/register")
    public String registerPost(@ModelAttribute("userDto") UserDto userDto, Model model) { //The parameter supplied to @ModelAttribute is INCREDIBLY IMPORTANT
        User user = userService.findByUsername(userDto.getUsername());
        if (user != null) {
            model.addAttribute("userAlreadyExists", user);
            return "redirect:/register?error";
        }
        user = userService.save(userDto);
        model.addAttribute("user", user);
        return "registration_success";
    }

    @GetMapping("/profile/{username}")
    public String profile(@PathVariable String username, Model model, @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User profile = userService.findByUsername(username);
        model.addAttribute("userExists", profile != null);
        model.addAttribute("profile", profile);

        if (profile != null) {
//            List<Game> games = profile.getGameIds().stream().map(gameStoreService::getGame).filter(Objects::nonNull).toList();
            List<Game> games = gameService.getGamesForPlayer(username);
            System.out.println("games: " + games);
            model.addAttribute("games", games);
        }

        model.addAttribute("authenticated", customUserDetails != null);
        if (customUserDetails != null) {
            model.addAttribute("user", userService.findByUsername(customUserDetails.getUsername()));
        }

        System.out.println("PROFILE user: " + model.getAttribute("user"));
        System.out.println("PROFILE authenticated: " + model.getAttribute("authenticated"));
        System.out.println("PROFILE customUserDetails: " + customUserDetails);
        System.out.println("PROFILE profile: " + model.getAttribute("profile"));
        return "profile";
    }

}
