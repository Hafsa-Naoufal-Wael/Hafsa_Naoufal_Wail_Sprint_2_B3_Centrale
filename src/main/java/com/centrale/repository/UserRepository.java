package com.centrale.repository;

import java.util.List;
import java.util.Optional;

import com.centrale.model.entity.User;

public interface UserRepository {
    User save(User user);
    Optional<User> findById(Long id);
    List<User> findAll();
    void delete(User user);
    Optional<User> findByEmail(String email);
}
