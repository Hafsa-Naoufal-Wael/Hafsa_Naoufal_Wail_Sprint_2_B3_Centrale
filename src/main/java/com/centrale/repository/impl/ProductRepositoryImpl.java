package com.centrale.repository.impl;

import com.centrale.model.entity.Product;
import com.centrale.repository.ProductRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductRepositoryImpl implements ProductRepository {

    @PersistenceContext(unitName = "ecommercePU")
    private EntityManager entityManager;

    @Override
    public List<Product> findAll() {
        TypedQuery<Product> query = entityManager.createQuery("SELECT p FROM Product p", Product.class);
        return query.getResultList();
    }

    @Override
    public Optional<Product> findById(Long id) {
        Product product = entityManager.find(Product.class, id);
        return Optional.ofNullable(product);
    }

    @Override
    public Product findByName(String name) {
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.name = :name", Product.class);
        query.setParameter("name", name);
        List<Product> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public List<Product> findByPriceLessThan(BigDecimal price) {
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.price < :price", Product.class);
        query.setParameter("price", price);
        return query.getResultList();
    }

    @Override
    public List<Product> findByStockGreaterThan(Integer stock) {
        TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.stock > :stock", Product.class);
        query.setParameter("stock", stock);
        return query.getResultList();
    }

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            entityManager.persist(product);
        } else {
            product = entityManager.merge(product);
        }
        return product;
    }

    @Override
    public void deleteById(Long id) {
        Product product = entityManager.find(Product.class, id);
        if (product != null) {
            entityManager.remove(product);
        }
    }
}

