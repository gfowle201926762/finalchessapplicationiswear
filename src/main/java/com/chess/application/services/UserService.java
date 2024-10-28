package com.chess.application.services;

import com.chess.application.model.User;
import com.chess.application.model.UserDTO;

public interface UserService {
    User findByUsername(String username);
    User save(UserDTO userDTO);
}
