package com.centrale.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.centrale.config.ThymeleafConfig;
import com.centrale.model.entity.Product;
import com.centrale.repository.ProductRepository;
import com.centrale.repository.impl.ProductRepositoryImpl;
import com.centrale.service.ProductService;

public class HomeController extends HttpServlet {
    private ProductService productService;
    private static final int PAGE_SIZE = 9; // Number of products per page

    @Override
    public void init() throws ServletException {
        super.init();
        ProductRepository productRepository = new ProductRepositoryImpl();
        productService = new ProductService(productRepository);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = 1;
        String pageParam = request.getParameter("page");
        String search = request.getParameter("search");

        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                page = Integer.parseInt(pageParam);
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                // If the page parameter is not a valid number, default to page 1
            }
        }

        List<Product> products = productService.searchProductsPaginated(search, page, PAGE_SIZE);
        int totalProducts = productService.getTotalProductCount(search);
        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);

        TemplateEngine templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
        WebContext context = new WebContext(request, response, getServletContext());

        context.setVariable("pageTitle", "Welcome to Central");
        context.setVariable("content", "index");
        context.setVariable("products", products);
        context.setVariable("currentPage", page);
        context.setVariable("totalPages", totalPages);
        context.setVariable("search", search);

        String result = templateEngine.process("layouts/main-layout", context);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(result);
    }
}
