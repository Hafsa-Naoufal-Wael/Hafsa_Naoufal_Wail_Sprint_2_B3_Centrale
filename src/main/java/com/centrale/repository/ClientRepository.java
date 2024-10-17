package com.centrale.repository;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import com.centrale.model.entity.Client;
import com.centrale.model.entity.User;

public interface ClientRepository {
    Client save(Client client);
    Optional<Client> findById(Long id);
    List<Client> findAll();
    void delete(Client client);
    List<Client> findAllPaginated(int page, int size);
    Session getSession();
    Client findByUser(User user);
    Client findByEmail(String email);
    Client saveWithSession(Client client, Session session);
}