package com.centrale.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import com.centrale.model.entity.Admin;
import com.centrale.model.entity.User;

public interface AdminRepository {
    Admin save(Admin admin);
    Optional<Admin> findById(Long id);
    List<Admin> findAll();
    void delete(Admin admin);
    List<Admin> findAllPaginated(int page, int size);
    Session getSession();
    Admin findByUser(User user);
}