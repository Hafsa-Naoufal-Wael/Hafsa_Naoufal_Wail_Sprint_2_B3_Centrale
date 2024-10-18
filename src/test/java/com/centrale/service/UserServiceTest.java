package com.centrale.service;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.centrale.model.entity.User;
import com.centrale.repository.UserRepository;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        userService = new UserService(userRepository);
    }

    @Test
    public void testGetUserById() {
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserById(userId);

        assertTrue(foundUser.isPresent());
        assertEquals(userId, foundUser.get().getId());
        verify(userRepository).findById(userId);
    }

    @Test
    public void testGetUserByEmail() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        Optional<User> foundUser = userService.getUserByEmail(email);

        assertTrue(foundUser.isPresent());
        assertEquals(email, foundUser.get().getEmail());
        verify(userRepository).findByEmail(email);
    }

    @Test
    public void testGetUsersPaginated() {
        int page = 1;
        int pageSize = 10;
        String search = "test";
        List<User> users = Arrays.asList(new User(), new User());
        when(userRepository.getUsersPaginated((page - 1) * pageSize, pageSize, search)).thenReturn(users);

        List<User> result = userService.getUsersPaginated(page, pageSize, search);

        assertEquals(users, result);
        verify(userRepository).getUsersPaginated((page - 1) * pageSize, pageSize, search);
    }
}