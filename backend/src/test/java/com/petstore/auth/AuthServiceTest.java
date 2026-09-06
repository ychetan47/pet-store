package com.petstore.auth;

import com.petstore.auth.dto.AuthResponse;
import com.petstore.auth.dto.LoginRequest;
import com.petstore.auth.dto.RegisterRequest;
import com.petstore.auth.entity.User;
import com.petstore.auth.repository.UserRepository;
import com.petstore.auth.security.JwtTokenProvider;
import com.petstore.auth.service.AuthService;
import com.petstore.common.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Chetan", "chetan@example.com", "encodedPassword", "9876543210");
        user.setId(1L);
    }

    @Test
    void register_validRequest_success() {
        RegisterRequest request = new RegisterRequest("Chetan", "chetan@example.com", "password123", "9876543210");

        when(userRepository.existsByEmail("chetan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(tokenProvider.generateTokenFromUser(1L, "chetan@example.com", "Chetan")).thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Chetan", response.getUser().getName());
        assertEquals("chetan@example.com", response.getUser().getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsBadRequestException() {
        RegisterRequest request = new RegisterRequest("Chetan", "chetan@example.com", "password123", "9876543210");

        when(userRepository.existsByEmail("chetan@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_success() {
        LoginRequest request = new LoginRequest("chetan@example.com", "password123");

        Authentication mockAuth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(tokenProvider.generateToken(mockAuth)).thenReturn("mock-jwt-token");
        when(userRepository.findByEmail("chetan@example.com")).thenReturn(Optional.of(user));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Chetan", response.getUser().getName());
    }

    @Test
    void login_invalidPassword_throwsBadCredentialsException() {
        LoginRequest request = new LoginRequest("chetan@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
