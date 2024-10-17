package com.centrale.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.centrale.model.entity.Client;
import com.centrale.model.entity.User;
import com.centrale.repository.ClientRepository;
import com.centrale.util.HibernateUtil;

public class ClientRepositoryImpl implements ClientRepository {

    private final SessionFactory sessionFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRepositoryImpl.class);
    public ClientRepositoryImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    @Override
    public Client save(Client client) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(client);
            session.getTransaction().commit();
            return client;
        } catch (Exception e) {
            LOGGER.error("Error saving client", e);
            throw e;
        }
    }

    @Override
    public Optional<Client> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Client.class, id));
        }
    }

    @Override
    public List<Client> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Client", Client.class).list();
        }
    }
    @Override
    public void delete(Client client) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(client);
            session.getTransaction().commit();
        }
    }

    @Override
    public List<Client> findAllPaginated(int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            Query<Client> query = session.createQuery("FROM Client", Client.class);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.list();
        }
    }

    @Override
    public Client findByUser(User user) {
        try (Session session = sessionFactory.openSession()) {
            Query<Client> query = session.createQuery("FROM Client c WHERE c.id = :userId", Client.class);
            query.setParameter("userId", user.getId());
            return query.uniqueResult();
        }
    }
    @Override
    public Client findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<Client> query = session.createQuery(
                "SELECT c FROM Client c JOIN c.user u WHERE u.email = :email", Client.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }
    public Client saveWithSession(Client client, Session session) {
        try {
            session.save(client);
            return client;
        } catch (Exception e) {
            LOGGER.error("Error saving client", e);
            throw e;
        }
    }
    @Override
    public Session getSession() {
        return sessionFactory.openSession();
    }
}
