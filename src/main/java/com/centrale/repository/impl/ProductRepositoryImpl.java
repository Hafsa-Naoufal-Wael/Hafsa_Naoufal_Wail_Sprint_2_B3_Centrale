package com.centrale.repository.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import com.centrale.model.entity.Product;
import com.centrale.repository.ProductRepository;
import com.centrale.util.HibernateUtil;

public class ProductRepositoryImpl implements ProductRepository {

    private final SessionFactory sessionFactory;
<<<<<<< HEAD

   public ProductRepositoryImpl() {
    this.sessionFactory = HibernateUtil.getSessionFactory();
}

=======

    public ProductRepositoryImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

>>>>>>> 5e092b93abc5ff3dd05cd49fb4fcfaca1e084e4d
    @Override
    public Product save(Product product) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(product);
            session.getTransaction().commit();
            return product;
<<<<<<< HEAD
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Product.class, id));
        }
    }

    @Override
    public List<Product> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Product", Product.class).list();
        }
    }

    @Override
    public void delete(Product product) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(product);
            session.getTransaction().commit();
        }
    }

    @Override
    public Product findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("FROM Product WHERE name = :name", Product.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        }
    }

    @Override
    public List<Product> findByPriceLessThan(BigDecimal price) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("FROM Product WHERE price < :price", Product.class);
            query.setParameter("price", price);
            return query.list();
=======
>>>>>>> 5e092b93abc5ff3dd05cd49fb4fcfaca1e084e4d
        }
    }

    @Override
<<<<<<< HEAD
    public List<Product> findByStockGreaterThan(Integer minStock) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("FROM Product WHERE stock > :minStock", Product.class);
            query.setParameter("minStock", minStock);
            return query.list();
        }
    }
=======
    public Optional<Product> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Product.class, id));
        }
    }

    @Override
    public List<Product> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM Product", Product.class).list();
        }
    }

    @Override
    public void delete(Product product) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.delete(product);
            session.getTransaction().commit();
        }
    }

    @Override
    public Product findByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("FROM Product WHERE name = :name", Product.class);
            query.setParameter("name", name);
            return query.uniqueResult();
        }
    }

    @Override
    public List<Product> findByPriceLessThan(BigDecimal price) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("FROM Product WHERE price < :price", Product.class);
            query.setParameter("price", price);
            return query.list();
        }
    }

    @Override
    public List<Product> findByStockGreaterThan(Integer minStock) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("FROM Product WHERE stock > :minStock", Product.class);
            query.setParameter("minStock", minStock);
            return query.list();
        }
    }
>>>>>>> 5e092b93abc5ff3dd05cd49fb4fcfaca1e084e4d
    @Override
    public List<Product> findAllPaginated(int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("FROM Product", Product.class);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.list();
        }
    }
<<<<<<< HEAD
    
=======

>>>>>>> 5e092b93abc5ff3dd05cd49fb4fcfaca1e084e4d
    @Override
    public int count() {
        try (Session session = sessionFactory.openSession()) {
            return ((Long) session.createQuery("SELECT COUNT(*) FROM Product").uniqueResult()).intValue();
        }
    }
    @Override
    public Session getSession() {
        return sessionFactory.openSession();
    }
}

