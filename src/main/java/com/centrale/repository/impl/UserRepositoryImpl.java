package com.centrale.repository.impl;

import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.centrale.model.entity.User;
import com.centrale.repository.UserRepository;
import com.centrale.util.HibernateUtil;

public class UserRepositoryImpl implements UserRepository {

    private final SessionFactory sessionFactory;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRepositoryImpl.class);

    public UserRepositoryImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }
    @Override
    public List<User> getUsersPaginated(int offset, int pageSize, String search) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try {
                String hql = "FROM User u WHERE 1=1";
                if (search != null && !search.isEmpty()) {
                    hql += " AND (u.firstName LIKE :search OR u.lastName LIKE :search OR u.email LIKE :search)";
                }
                hql += " ORDER BY u.id";

                Query<User> query = session.createQuery(hql, User.class);
                if (search != null && !search.isEmpty()) {
                    query.setParameter("search", "%" + search + "%");
                }
                query.setFirstResult(offset);
                query.setMaxResults(pageSize);

                List<User> result = query.getResultList();
                session.getTransaction().commit();
                return result;
            } catch (Exception e) {
                session.getTransaction().rollback();
                LOGGER.error("Error fetching paginated users", e);
                throw e;
            }
        }
    }

    @Override
    public int getTotalUsersCount(String search) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            try {
                String hql = "SELECT COUNT(u) FROM User u WHERE 1=1";
                if (search != null && !search.isEmpty()) {
                    hql += " AND (u.firstName LIKE :search OR u.lastName LIKE :search OR u.email LIKE :search)";
                }

                Query<Long> query = session.createQuery(hql, Long.class);
                if (search != null && !search.isEmpty()) {
                    query.setParameter("search", "%" + search + "%");
                }

                Long count = query.uniqueResult();
                session.getTransaction().commit();
                return count.intValue();
            } catch (Exception e) {
                session.getTransaction().rollback();
                LOGGER.error("Error getting total users count", e);
                throw e;
            }
        }
    }
    @Override
    public User save(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(user);
            session.getTransaction().commit();
            return user;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(User.class, id));
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM User", User.class).list();
        }
    }

    @Override
    public void delete(User user) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(user);
            session.getTransaction().commit();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try (Session session = sessionFactory.openSession()) {
            Query<User> query = session.createQuery("FROM User WHERE email = :email", User.class);
            query.setParameter("email", email);
            return Optional.ofNullable(query.uniqueResult());
        }
    }
}
