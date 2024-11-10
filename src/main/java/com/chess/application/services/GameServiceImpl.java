package com.chess.application.services;

import com.chess.application.controller.model.Move;
import com.chess.application.model.Game;
import com.chess.application.model.ReturnPayload;
import com.chess.application.model.Status;
import com.chess.application.repositories.GameRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final EntityManager entityManager;

    @Autowired
    public GameServiceImpl(GameRepository gameRepository, EntityManager entityManager) {
        this.gameRepository = gameRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Game getGame(String id) {
        System.out.println("GETTING GAME: " + id);
        // prevent Hibernate's multipleBagException -> can fix with a Set but that won't get rid of the Cartesian product.
        Game game = entityManager.createQuery("""
            select g
            from Game g
            left join fetch g.fenStrings
            where g.uuid = :gameId
            """, Game.class)
            .setParameter("gameId", id)
            .getSingleResult();

        List<String> fenStrings = game.getFenStrings();

        List<Long> hashValues = entityManager.createQuery("""
            select g
            from Game g
            left join fetch g.hashValues
            where g.uuid = :gameId
            """, Game.class)
            .setParameter("gameId", id)
            .getSingleResult().getHashValues(); //.toBuilder().fenStrings(fenStrings).build();

        game = entityManager.createQuery("""
            select g
            from Game g
            left join fetch g.moves
            where g.uuid = :gameId
            """, Game.class)
            .setParameter("gameId", id)
            .getSingleResult().toBuilder().fenStrings(fenStrings).hashValues(hashValues).build();

        return game;
    }

    public List<Game> getGamesForPlayer(String id) {
        return gameRepository.findByWhiteIdOrBlackId(id, id);
    }

    @Transactional
    public void updateGame(String id, Status status, Status.Reason reason) {
        Game game = getGame(id);
        if (game == null) {
            throw new RuntimeException("Game not found for id " + id);
        }
        gameRepository.save(game.toBuilder().status(status).reason(reason).build());
    }

    @Transactional // prevents LazyInitialisation exception
    public void updateGame(String id, Move move) {
        Game game = getGame(id);
        if (game == null) {
            throw new RuntimeException("Game not found for id " + id);
        }
        gameRepository.save(game.addMove(move));
    }

    @Transactional
    public void updateGame(String id, ReturnPayload returnPayload) {
        Game game = getGame(id);
        if (game == null) {
            throw new RuntimeException("Game not found for id " + id);
        }
        if (returnPayload.getFenStringClient() != null && !returnPayload.getFenStringClient().isEmpty()) {
            game.addFenString(returnPayload.getFenStringClient());
        }
        if (returnPayload.getFenStringEngine() != null && !returnPayload.getFenStringEngine().isEmpty()) {
            game.addFenString(returnPayload.getFenStringEngine());
        }
        if (returnPayload.getMoveHashClient() != 0) {
            game.addHashValue(returnPayload.getMoveHashClient());
        }
        if (returnPayload.getMoveHashEngine() != 0) {
            game.addHashValue(returnPayload.getMoveHashEngine());
        }
        if (returnPayload.getResponse() != null) {
            game.addMove(returnPayload.getResponse());
        }
        if (returnPayload.getStatus() != null) {
            game.setStatus(returnPayload.getStatus());
        }
        if (returnPayload.getReason() != null) {
            game.setReason(returnPayload.getReason());
        }
        gameRepository.save(game);
    }

    public Game createNewGame(Game game) {
        // assuming playerId, opponentId, and Settings already sent in the payload.
        String id = UUID.randomUUID().toString();
        List<Long> hashValues = new ArrayList<>();
        List<String> fenList = new ArrayList<>();
        fenList.add("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        ZonedDateTime now = ZonedDateTime.now();
        System.out.println("now: " + now);
        Game initialisedGame = game.toBuilder()
            .uuid(id)
            .fenStrings(fenList)
            .status(Status.ONGOING)
            .hashValues(hashValues)
            .zonedDateTime(now)
            .build();
        gameRepository.save(initialisedGame);
        System.out.println("SAVED GAME TO REPOSITORY: " + initialisedGame.getUuid());
        return initialisedGame;
    }
}
