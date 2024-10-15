package com.centrale.service;

import com.centrale.model.entity.Product;
import com.centrale.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ProductService {

    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
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

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public Product updateProductStock(Long id, Integer newStock) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStock(newStock);
            return productRepository.save(product);
        }
        throw new RuntimeException("Product not found with id: " + id);
    }

    public Product updateProductPrice(Long id, BigDecimal newPrice) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setPrice(newPrice);
            return productRepository.save(product);
        }
        throw new RuntimeException("Product not found with id: " + id);
    }
}