package com.centrale.service;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.centrale.model.entity.Product;
import com.centrale.repository.ProductRepository;

public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        productService = new ProductService(productRepository);
    }

    @Test
    public void testGetProductById() {
        Long productId = 1L;
        Product product = new Product("Test Product", "Description", 10.0, 100);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        Optional<Product> foundProduct = productService.getProductById(productId);

        assertTrue(foundProduct.isPresent());
        assertEquals(product, foundProduct.get());
        verify(productRepository).findById(productId);
    }

    @Test
    public void testGetProductsBelowPrice() {
        BigDecimal price = BigDecimal.valueOf(15.0);
        List<Product> products = Arrays.asList(
            new Product("Product 1", "Description 1", 10.0, 100),
            new Product("Product 2", "Description 2", 14.99, 200)
        );
        when(productRepository.findByPriceLessThan(price)).thenReturn(products);

        List<Product> foundProducts = productService.getProductsBelowPrice(price);

        assertEquals(products, foundProducts);
        verify(productRepository).findByPriceLessThan(price);
    }

    @Test
    public void testSearchProductsPaginated() {
        String search = "test";
        int page = 1;
        int pageSize = 10;
        List<Product> products = Arrays.asList(
            new Product("Test Product 1", "Description 1", 10.0, 100),
            new Product("Test Product 2", "Description 2", 20.0, 200)
        );
        when(productRepository.findAllPaginated(page, pageSize, search)).thenReturn(products);

        List<Product> foundProducts = productService.searchProductsPaginated(search, page, pageSize);

        assertEquals(products, foundProducts);
        verify(productRepository).findAllPaginated(page, pageSize, search);
    }
}