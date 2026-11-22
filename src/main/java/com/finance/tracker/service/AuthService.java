package com.finance.tracker.service;

import com.finance.tracker.dto.request.LoginRequest;
import com.finance.tracker.dto.request.RegisterRequest;
import com.finance.tracker.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
