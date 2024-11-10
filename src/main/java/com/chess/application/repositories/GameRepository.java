package com.chess.application.repositories;

import com.chess.application.model.Game;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRepository extends JpaRepository<Game, Long> {
    @EntityGraph(attributePaths = {"fenStrings", "hashValues", "moves"})
    Game findByUuid(String id); // uses method name to find
    Game save(Game game);
    List<Game> findByWhiteIdOrBlackId(String whiteId, String blackId);
}
