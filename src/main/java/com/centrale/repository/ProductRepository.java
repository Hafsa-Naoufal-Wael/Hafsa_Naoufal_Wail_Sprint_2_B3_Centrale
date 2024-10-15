package com.centrale.repository;

import com.centrale.model.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
        List<Product> findAll();
        Optional<Product> findById(Long id);
        Product findByName(String name);
        List<Product> findByPriceLessThan(BigDecimal price);
        List<Product> findByStockGreaterThan(Integer stock);
        Product save(Product product);
        void deleteById(Long id);
}