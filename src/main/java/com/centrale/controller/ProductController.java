package com.centrale.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
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
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProductController extends HttpServlet {

    private ProductService productService;
    private ObjectMapper objectMapper;
    private static final Logger LOGGER = Logger.getLogger(ProductController.class.getName());

    @Override
    public void init() throws ServletException {
        super.init();
        ProductRepository productRepository = new ProductRepositoryImpl();
        productService = new ProductService(productRepository);
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();

        if ("/admin/products".equals(servletPath)) {
            handleAdminGet(request, response, pathInfo);
        } else if ("/product".equals(servletPath)) {
            handlePublicGet(request, response, pathInfo);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String pathInfo = request.getPathInfo();
        String servletPath = request.getServletPath();

        if ("/admin/products".equals(servletPath)) {
            handleAdminPost(request, response, pathInfo);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleAdminGet(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        if ("/get".equals(pathInfo)) {
            getProduct(request, response);
        } else {
            showProducts(request, response);
        }
    }

    private void handlePublicGet(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        if (pathInfo == null || pathInfo.equals("/")) {
            showProducts(request, response);
        } else {
            showProductDetails(request, response, pathInfo);
        }
    }

    private void handleAdminPost(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        if ("/add".equals(pathInfo)) {
            addProduct(request, response);
        } else if ("/update".equals(pathInfo)) {
            updateProduct(request, response);
        } else if ("/delete".equals(pathInfo)) {
            deleteProduct(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void showProducts(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int page = 1;
        int pageSize = 10;
        String search = request.getParameter("search");

        if (request.getParameter("page") != null) {
            page = Integer.parseInt(request.getParameter("page"));
        }

        List<Product> products = productService.getProductsPaginated(page, pageSize, search);
        int totalProducts = productService.getTotalProductsCount(search);
        int totalPages = (int) Math.ceil((double) totalProducts / pageSize);

        ServletContext servletContext = getServletContext();
        TemplateEngine engine = ThymeleafConfig.getTemplateEngine(servletContext);

        WebContext context = new WebContext(request, response, servletContext);
        context.setVariable("products", products);
        context.setVariable("currentPage", page);
        context.setVariable("totalPages", totalPages);
        context.setVariable("pageTitle", "Products");

        String template = request.getServletPath().startsWith("/admin") ? "admin/manage-products" : "product/list";
        engine.process(template, context, response.getWriter());
    }

    private void showProductDetails(HttpServletRequest request, HttpServletResponse response, String pathInfo)
            throws ServletException, IOException {
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length == 2) {
            try {
                Long productId = Long.parseLong(pathParts[1]);
                Optional<Product> product = productService.getProductById(productId);
                if (product.isPresent()) {
                    ServletContext servletContext = getServletContext();
                    TemplateEngine engine = ThymeleafConfig.getTemplateEngine(servletContext);

                    WebContext context = new WebContext(request, response, servletContext);
                    context.setVariable("product", product.get());
                    context.setVariable("pageTitle", product.get().getName());

                    engine.process("product/details", context, response.getWriter());
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
                }
            } catch (NumberFormatException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid product ID");
            }
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void getProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long id = Long.parseLong(request.getParameter("id"));
        Optional<Product> product = productService.getProductById(id);

        if (product.isPresent()) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(objectMapper.writeValueAsString(product.get()));
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void addProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.warning("Received request to add product");
        LOGGER.warning("Request parameters: " + request.getParameterMap());
        String name = request.getParameter("name");
        LOGGER.warning("Adding product with name: " + name);
        if (name == null || name.trim().isEmpty()) {
            LOGGER.warning("Product name is null or empty");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Product name is required");
            return;
        }
        String description = request.getParameter("description");
        String priceStr = request.getParameter("price");
        String stockStr = request.getParameter("stock");
        LOGGER.warning("Price: " + priceStr + ", Stock: " + stockStr);

        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);

            Product newProduct = new Product(name, description, price, stock);
            productService.saveProduct(newProduct);

            LOGGER.warning("Product added successfully: " + newProduct);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Product added successfully");
        } catch (NumberFormatException e) {
            LOGGER.warning("Error parsing price or stock: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid price or stock value");
        }
    }

    private void updateProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idParam = request.getParameter("id");
        LOGGER.warning("Updating product with ID: " + idParam);
        if (idParam == null || idParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Product ID is required");
            return;
        }
        Long id = Long.parseLong(idParam);
        String name = request.getParameter("name");
        String description = request.getParameter("description");
        String priceStr = request.getParameter("price");
        String stockStr = request.getParameter("stock");
        LOGGER.warning("Price: " + priceStr + ", Stock: " + stockStr);
        try {
            double price = Double.parseDouble(priceStr);
            int stock = Integer.parseInt(stockStr);

            Product updatedProduct = new Product(name, description, price, stock);
            updatedProduct.setId(id);
            productService.updateProduct(updatedProduct);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Product updated successfully");
        } catch (NumberFormatException e) {
            LOGGER.warning("Error parsing price or stock: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid price or stock value");
        }
    }

    private void deleteProduct(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LOGGER.warning("Deleting product with ID: " + request.getParameter("id"));
        Long id = Long.parseLong(request.getParameter("id"));
        Optional<Product> productOptional = productService.getProductById(id);

        if (productOptional.isPresent()) {
            try {
                productService.deleteProduct(productOptional.get());
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                LOGGER.warning("Error deleting product: " + e.getMessage());
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                response.getWriter().write("Cannot delete product. It is referenced in existing orders.");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Product not found");
        }
    }
}
