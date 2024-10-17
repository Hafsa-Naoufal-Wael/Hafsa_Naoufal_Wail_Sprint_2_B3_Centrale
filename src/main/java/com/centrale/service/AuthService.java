package com.centrale.service;

import com.centrale.model.entity.Client;
import com.centrale.model.entity.User;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.UserRepository;
import com.centrale.repository.ClientRepository;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Optional;

public class AuthService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    public AuthService(UserRepository userRepository, ClientRepository clientRepository) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
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
    public User register(String firstName, String lastName, String email, String password) throws Exception {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email already exists");
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        user.setRole(UserRole.CLIENT);

        user = userRepository.save(user);

        Client client = new Client();
        client.setUser(user);
        // Remove this line: client.setId(user.getId());

        clientRepository.save(client);

        return user;
    }
}