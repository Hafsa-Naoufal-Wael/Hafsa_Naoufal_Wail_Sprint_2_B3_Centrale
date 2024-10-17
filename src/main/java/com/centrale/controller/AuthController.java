package com.centrale.controller;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.centrale.config.ThymeleafConfig;
import com.centrale.model.entity.Client;
import com.centrale.model.entity.User;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.impl.ClientRepositoryImpl;
import com.centrale.repository.impl.UserRepositoryImpl;
import com.centrale.service.AuthService;
import com.centrale.service.ClientService;

public class    AuthController extends HttpServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private AuthService authService;
    private ClientService clientService;
    private TemplateEngine templateEngine;

    @Override
    public void init() throws ServletException {
        super.init();
        authService = new AuthService(new UserRepositoryImpl(), new ClientRepositoryImpl());
        clientService = new ClientService(new ClientRepositoryImpl());
        templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if (action == null) {
            action = "/login";
        }
        switch (action) {
            case "/login":
                showLoginForm(request, response);
                break;
            case "/register":
                showRegisterForm(request, response);
                break;
            case "/logout":
                logout(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getPathInfo();
        if (action == null) {
            action = "/login";
        }
        switch (action) {
            case "/login":
                processLogin(request, response);
                break;
            case "/register":
                processRegister(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showLoginForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Login");
        context.setVariable("error", request.getParameter("error"));
        templateEngine.process("auth/login", context, response.getWriter());
    }

    private void showRegisterForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebContext context = new WebContext(request, response, getServletContext());
        context.setVariable("pageTitle", "Register");
        context.setVariable("error", request.getParameter("error"));
        templateEngine.process("auth/register", context, response.getWriter());
    }

    private void processLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            Optional<User> userOptional = authService.login(email, password);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                HttpSession session = request.getSession();

                // Set user object in session
                session.setAttribute("user", user);

                // Set user role in session
                session.setAttribute("userRole", user.getRole());

                if (user.getRole() == UserRole.CLIENT) {
                    Client client = clientService.getClientByEmail(email);
                    if (client != null) {
                        // Set client object in session for CLIENT role
                        session.setAttribute("client", client);
                    } else {
                        LOGGER.error("Client data not found for user: {}", email);
                        response.sendRedirect(request.getContextPath() + "/auth/login?error=Client+profile+not+found");
                        return;
                    }
                }

                LOGGER.info("User logged in: {} with role: {}", user.getEmail(), user.getRole());
                response.sendRedirect(request.getContextPath() + "/");
            } else {
                response.sendRedirect(request.getContextPath() + "/auth/login?error=Invalid+email+or+password");
            }
        } catch (Exception e) {
            LOGGER.error("Error during login", e);
            response.sendRedirect(request.getContextPath() + "/auth/login?error=An+error+occurred+during+login");
        }
    }

    private void processRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User registeredUser = authService.register(firstName, lastName, email, password);
            if (registeredUser != null) {
                response.sendRedirect(request.getContextPath() + "/auth/login?success=Registration+successful");
            } else {
                response.sendRedirect(request.getContextPath() + "/auth/register?error=Registration+failed");
            }
        } catch (Exception e) {
            LOGGER.error("Error during registration", e);
            response.sendRedirect(request.getContextPath() + "/auth/register?error=" + e.getMessage());
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/");
    }
}
