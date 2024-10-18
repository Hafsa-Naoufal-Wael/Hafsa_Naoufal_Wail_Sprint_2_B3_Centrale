package com.centrale.service;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.centrale.model.entity.Client;
import com.centrale.model.entity.User;
import com.centrale.model.enums.UserRole;
import com.centrale.repository.ClientRepository;
import com.centrale.repository.UserRepository;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClientRepository clientRepository;

    private AuthService authService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        authService = new AuthService(userRepository, clientRepository);
    }

    @Test
    public void testLoginSuccess() {
        String email = "test@example.com";
        String password = "password";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> loggedInUser = authService.login(email, password);

        assertTrue(loggedInUser.isPresent());
        assertEquals(email, loggedInUser.get().getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void testLoginFailure() {
        String email = "test@example.com";
        String password = "password";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<User> loggedInUser = authService.login(email, password);

        assertFalse(loggedInUser.isPresent());
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void testRegister() throws Exception {
        String firstName = "John";
        String lastName = "Doe";
        String email = "john@example.com";
        String password = "password";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        User registeredUser = authService.register(firstName, lastName, email, password);

        assertNotNull(registeredUser);
        assertEquals(firstName, registeredUser.getFirstName());
        assertEquals(lastName, registeredUser.getLastName());
        assertEquals(email, registeredUser.getEmail());
        assertEquals(UserRole.CLIENT, registeredUser.getRole());
        assertTrue(BCrypt.checkpw(password, registeredUser.getPassword()));
        verify(userRepository).findByEmail(email);
        verify(userRepository).save(any(User.class));
        verify(clientRepository).save(any(Client.class));
    }

    @Test(expected = Exception.class)
    public void testRegisterWithExistingEmail() throws Exception {
        String email = "existing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

        authService.register("John", "Doe", email, "password");
    }
}