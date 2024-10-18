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

    public ProductRepositoryImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory();
    }

    @Override
    public Product save(Product product) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.saveOrUpdate(product);
            session.getTransaction().commit();
            return product;
        }
    }
    @Override
    public Product update(Product product) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(product);
            session.getTransaction().commit();
            return product;
        }
    }
    @Override
    public Optional<Product> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Product.class, id));
        }
    }
    @Override
    public List<Product> findAllPaginated(int page, int pageSize, String search) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Product p WHERE 1=1";
            if (search != null && !search.isEmpty()) {
                hql += " AND (p.name LIKE :search OR p.description LIKE :search)";
            }
            hql += " ORDER BY p.id";
            Query<Product> query = session.createQuery(hql, Product.class);
            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }
            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);
            return query.getResultList();
        }
    }
    @Override
    public int getTotalProductCount(String search) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "SELECT COUNT(*) FROM Product p WHERE 1=1";
            if (search != null && !search.isEmpty()) {
                hql += " AND (p.name LIKE :search OR p.description LIKE :search)";
            }
            Query<Long> query = session.createQuery(hql, Long.class);
            if (search != null && !search.isEmpty()) {
                query.setParameter("search", "%" + search + "%");
            }
            return query.uniqueResult().intValue();
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
    @Override
    public List<Product> findAllPaginated(int page, int size) {
        try (Session session = sessionFactory.openSession()) {
            Query<Product> query = session.createQuery("FROM Product", Product.class);
            query.setFirstResult((page - 1) * size);
            query.setMaxResults(size);
            return query.list();
        }
    }

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

