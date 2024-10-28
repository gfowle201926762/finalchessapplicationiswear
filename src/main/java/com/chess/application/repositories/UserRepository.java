package com.chess.application.repositories;

import com.chess.application.model.User;
import com.chess.application.model.UserDTO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    User save(UserDTO userDto);
}