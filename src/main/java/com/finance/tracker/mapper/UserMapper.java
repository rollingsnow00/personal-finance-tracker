package com.finance.tracker.mapper;

import com.finance.tracker.dto.response.UserResponse;
import com.finance.tracker.model.User;

public final class UserMapper {
    private UserMapper() { }

    public static UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRoles());
    }
}
