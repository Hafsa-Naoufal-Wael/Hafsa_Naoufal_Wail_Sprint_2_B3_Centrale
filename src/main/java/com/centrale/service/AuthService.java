package com.centrale.service;

import com.centrale.model.entity.User;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(String firstName, String lastName, String email, String password, UserRole role) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole(role);
        return userRepository.save(user);
    }

    public Optional<User> login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (BCrypt.checkpw(password, user.getPassword())) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}
