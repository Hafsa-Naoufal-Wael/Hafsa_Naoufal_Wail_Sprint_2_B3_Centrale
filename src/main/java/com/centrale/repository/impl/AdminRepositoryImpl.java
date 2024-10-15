package com.centrale.repository.impl;

import com.centrale.model.entity.Admin;
import com.centrale.repository.AdminRepository;
import com.centrale.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class AdminRepositoryImpl implements AdminRepository {

    private final SessionFactory sessionFactory;

    public AdminRepositoryImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    @Override
    public Admin save(Admin admin) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(admin);
            session.getTransaction().commit();
            return admin;
        }
    }

    @Override
    public Optional<Admin> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Admin.class, id));
        }
    }

    @Override
    public List<Admin> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Admin", Admin.class).list();
        }
    }

    @Override
    public void delete(Admin admin) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(admin);
            session.getTransaction().commit();
        }
    }

    @Override
    public List<Admin> findAllPaginated(int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            Query<Admin> query = session.createQuery("FROM Admin", Admin.class);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.list();
        }
    }

    @Override
    public Session getSession() {
        return sessionFactory.openSession();
    }
}