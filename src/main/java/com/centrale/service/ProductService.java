package com.centrale.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.centrale.model.entity.Product;
import com.centrale.repository.ProductRepository;

public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void deleteProduct(Product product) {
        productRepository.delete(product);
    }

    public Product getProductByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> getProductsBelowPrice(BigDecimal price) {
        return productRepository.findByPriceLessThan(price);
    }

    public List<Product> getProductsInStock(Integer minStock) {
        return productRepository.findByStockGreaterThan(minStock);
    }
    public List<Product> getAllProductsPaginated(int page, int size) {
        return productRepository.findAllPaginated(page, size);
    }
    public int getTotalProductCount() {
        return productRepository.count();
    }
}
