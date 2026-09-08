package com.petstore.user.service;

import com.petstore.user.dto.AuthResponse;
import com.petstore.user.dto.LoginRequest;
import com.petstore.user.dto.RegisterRequest;
import com.petstore.user.entity.Role;
import com.petstore.user.entity.User;
import com.petstore.user.exception.DuplicateResourceException;
import com.petstore.user.repository.UserRepository;
import com.petstore.user.security.CustomUserDetails;
import com.petstore.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Chetan Sharma", "chetan@example.com", "encodedPassword", "9876543210", Role.CUSTOMER);
        sampleUser.setId(1L);
    }

    @Test
    @DisplayName("Should successfully register a new customer")
    void shouldRegisterNewCustomer() {
        RegisterRequest request = new RegisterRequest("Chetan Sharma", "chetan@example.com", "Password123!", "9876543210");

        when(userRepository.existsByEmail("chetan@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateToken(1L, "chetan@example.com", Role.CUSTOMER)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("chetan@example.com", response.getEmail());
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals(Role.CUSTOMER, response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration with duplicate email")
    void shouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest("Duplicate", "chetan@example.com", "Password123!", "9876543210");

        when(userRepository.existsByEmail("chetan@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully authenticate user with valid credentials")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest("chetan@example.com", "Password123!");
        CustomUserDetails userDetails = new CustomUserDetails(sampleUser);
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authToken);
        when(tokenProvider.generateToken(1L, "chetan@example.com", Role.CUSTOMER)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("chetan@example.com", response.getEmail());
    }

    @Test
    @DisplayName("Should reject login with invalid credentials")
    void shouldRejectInvalidCredentials() {
        LoginRequest request = new LoginRequest("chetan@example.com", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }
}
