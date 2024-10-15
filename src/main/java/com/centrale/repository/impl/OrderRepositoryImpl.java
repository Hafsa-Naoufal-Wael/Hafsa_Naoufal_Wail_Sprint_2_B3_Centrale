package com.centrale.repository.impl;

import com.centrale.model.entity.Order;
import com.centrale.model.entity.Client;
import com.centrale.model.enums.OrderStatus;
import com.centrale.repository.OrderRepository;
import com.centrale.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class OrderRepositoryImpl implements OrderRepository {

    private final SessionFactory sessionFactory;

    public OrderRepositoryImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    @Override
    public Order save(Order order) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(order);
            session.getTransaction().commit();
            return order;
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Order.class, id));
        }
    }

    @Override
    public List<Order> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Order", Order.class).list();
        }
    }

    @Override
    public void delete(Order order) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(order);
            session.getTransaction().commit();
        }
    }

    @Override
    public List<Order> findByClient(Client client) {
        try (Session session = sessionFactory.openSession()) {
            Query<Order> query = session.createQuery("FROM Order WHERE client = :client", Order.class);
            query.setParameter("client", client);
            return query.list();
        }
    }
    @Override
    public List<Order> findAllPaginated(int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            Query<Order> query = session.createQuery("FROM Order", Order.class);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.list();
        }
    }
    @Override
    public List<Order> findByStatus(OrderStatus status) {
        try (Session session = sessionFactory.openSession()) {
            Query<Order> query = session.createQuery("FROM Order WHERE status = :status", Order.class);
            query.setParameter("status", status);
            return query.list();
        }
    }
}

