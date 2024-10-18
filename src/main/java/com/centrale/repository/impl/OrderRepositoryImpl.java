package com.centrale.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.centrale.model.entity.Client;
import com.centrale.model.entity.Order;
import com.centrale.model.enums.OrderStatus;
import com.centrale.repository.OrderRepository;
import com.centrale.util.HibernateUtil;

public class OrderRepositoryImpl implements OrderRepository {

    private final SessionFactory sessionFactory;

    public OrderRepositoryImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }
    @Override
    public void update(Order order) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(order);
            session.getTransaction().commit();
        }
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
    public List<Order> findByStatus(OrderStatus status) {
        try (Session session = sessionFactory.openSession()) {
            Query<Order> query = session.createQuery("FROM Order WHERE status = :status", Order.class);
            query.setParameter("status", status);
            return query.list();
        }
    }

    @Override
    public List<Order> findRecentByClient(Client client, int limit) {
        try (Session session = sessionFactory.openSession()) {
            Query<Order> query = session.createQuery(
                "FROM Order WHERE client = :client ORDER BY orderDate DESC", Order.class);
            query.setParameter("client", client);
            query.setMaxResults(limit);
            return query.list();
        }
    }

    @Override
    public List<Order> findAllPaginated(int page, int pageSize, String search) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "FROM Order o WHERE 1=1";
            if (search != null && !search.isEmpty()) {
                hql += " AND (CAST(o.id AS string) LIKE :search OR LOWER(o.client.user.firstName) LIKE :search OR LOWER(o.client.user.lastName) LIKE :search OR LOWER(CAST(o.status AS string)) LIKE :search)";
            }
            hql += " ORDER BY o.orderDate DESC";
            
            Query<Order> query = session.createQuery(hql, Order.class);
            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            
            return query.getResultList();
        } finally {
            session.close();
        }
    }

    @Override
    public int getTotalOrderCount(String search) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "SELECT COUNT(o) FROM Order o WHERE 1=1";
            if (search != null && !search.isEmpty()) {
                hql += " AND (CAST(o.id AS string) LIKE :search OR LOWER(o.client.user.firstName) LIKE :search OR LOWER(o.client.user.lastName) LIKE :search OR LOWER(CAST(o.status AS string)) LIKE :search)";
            }
            Query<Long> query = session.createQuery(hql, Long.class);
            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search.toLowerCase() + "%");
            }
            return query.uniqueResult().intValue();
        } finally {
            session.close();
        }
    }

    @Override
    public List<Order> findByClientPaginated(Client client, int page, int pageSize, String searchTerm) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "FROM Order o WHERE o.client = :client";
            if (searchTerm != null && !searchTerm.isEmpty()) {
                hql += " AND (o.id LIKE :searchTerm OR o.status LIKE :searchTerm)";
            }
            hql += " ORDER BY o.orderDate DESC";
            
            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("client", client);
            if (searchTerm != null && !searchTerm.isEmpty()) {
                query.setParameter("searchTerm", "%" + searchTerm + "%");
            }
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            
            return query.list();
        } finally {
            session.close();
        }
    }

    @Override
    public int getTotalOrderCountByClient(Client client, String searchTerm) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "SELECT COUNT(o) FROM Order o WHERE o.client = :client";
            if (searchTerm != null && !searchTerm.isEmpty()) {
                hql += " AND (o.id LIKE :searchTerm OR o.status LIKE :searchTerm)";
            }
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("client", client);
            if (searchTerm != null && !searchTerm.isEmpty()) {
                query.setParameter("searchTerm", "%" + searchTerm + "%");
            }
            return query.uniqueResult().intValue();
        } finally {
            session.close();
        }
    }
    @Override
    public List<Order> getClientOrdersPaginated(Long clientId, int offset, int pageSize, String search) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "FROM Order o WHERE o.client.id = :clientId";
            if (search != null && !search.isEmpty()) {
                hql += " AND (o.id LIKE :search OR o.status LIKE :search)";
            }
            hql += " ORDER BY o.orderDate DESC";
            
            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("clientId", clientId);
            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }
            query.setFirstResult(offset);
            query.setMaxResults(pageSize);
            
            return query.list();
        } finally {
            session.close();
        }
    }

    @Override
    public int getTotalClientOrdersCount(Long clientId, String search) {
        Session session = sessionFactory.openSession();
        try {
            String hql = "SELECT COUNT(o) FROM Order o WHERE o.client.id = :clientId";
            if (search != null && !search.isEmpty()) {
                hql += " AND (o.id LIKE :search OR o.status LIKE :search)";
            }
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("clientId", clientId);
            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }
            return query.uniqueResult().intValue();
        } finally {
            session.close();
        }
    }
}
