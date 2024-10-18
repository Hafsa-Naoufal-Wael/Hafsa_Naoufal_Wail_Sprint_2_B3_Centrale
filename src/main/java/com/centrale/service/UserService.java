package com.centrale.service;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.centrale.model.entity.User;
import com.centrale.repository.UserRepository;

public class UserService {

    private final UserRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    public List<User> getUsersPaginated(int page, int pageSize, String search) {
        int offset = (page - 1) * pageSize;
        LOGGER.info("Getting paginated users - Offset: {}, PageSize: {}, Search: {}", offset, pageSize, search);
        List<User> users = userRepository.getUsersPaginated(offset, pageSize, search);
        LOGGER.info("Retrieved {} users", users.size());
        return users;
    }

    public int getTotalUsersCount(String search) {
        return userRepository.getTotalUsersCount(search);
    }
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
