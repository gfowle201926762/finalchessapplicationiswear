package com.chess.application.services;

import com.chess.application.model.User;
import com.chess.application.model.UserDto;
import com.chess.application.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User save(UserDto userDto) {
        User user = User.builder()
            .username(userDto.getUsername())
            .password(passwordEncoder.encode(userDto.getPassword()))
            .build();
        return userRepository.save(user);
    }

//    @Transactional
//    public void addGameIdToUser(String gameId, String username) {
//        User user = userRepository.findByUsername(username);
//        if (user == null) {
//            // must be a guest
//            return;
//        }
//        user.getGameIds().add(gameId);
//        userRepository.save(user);
//    }
}
