package com.centrale.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.hibernate.Session;

import com.centrale.model.entity.Product;

public interface ProductRepository {
        Product save(Product product);
        Optional<Product> findById(Long id);
        List<Product> findAll();
        void delete(Product product);
        Product findByName(String name);
        List<Product> findByPriceLessThan(BigDecimal price);
        List<Product> findByStockGreaterThan(Integer minStock);
        Session getSession();
        List<Product> findAllPaginated(int page, int size);
        int count();
}
