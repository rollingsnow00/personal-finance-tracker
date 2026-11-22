package com.finance.tracker.service;

import com.finance.tracker.exception.ResourceNotFoundException;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) { this.userRepository = userRepository; }

    public User require(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
    }
}
