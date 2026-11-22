package com.finance.tracker.service.impl;

import com.finance.tracker.dto.request.LoginRequest;
import com.finance.tracker.dto.request.RegisterRequest;
import com.finance.tracker.dto.response.AuthResponse;
import com.finance.tracker.dto.response.UserResponse;
import com.finance.tracker.exception.DuplicateResourceException;
import com.finance.tracker.mapper.UserMapper;
import com.finance.tracker.model.Role;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.UserRepository;
import com.finance.tracker.security.JwtService;
import com.finance.tracker.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Set;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Username is already registered");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        User user = userRepository.save(new User(username, email, passwordEncoder.encode(request.password()), Set.of(Role.USER)));
        UserDetails details = org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPasswordHash()).authorities("ROLE_USER").build();
        return issue(details, UserMapper.toResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.identifier().trim(), request.password()));
        UserDetails details = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsernameIgnoreCase(details.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated account was not found"));
        return issue(details, UserMapper.toResponse(user));
    }

    private AuthResponse issue(UserDetails details, UserResponse user) {
        JwtService.IssuedToken token = jwtService.issue(details);
        return new AuthResponse("Bearer", token.value(), token.expiresAt(), user);
    }
}
