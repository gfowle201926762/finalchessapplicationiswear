package com.chess.application.services;

import com.chess.application.model.User;
import com.chess.application.model.UserDto;

public interface UserService {
    User findByUsername(String username);
//    void addGameIdToUser(String gameId, String username);
    User save(UserDto userDto);
}
