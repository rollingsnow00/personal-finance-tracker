package com.finance.tracker.dto.response;

import com.finance.tracker.model.Role;

import java.util.Set;

public record UserResponse(Long id, String username, String email, Set<Role> roles) { }
