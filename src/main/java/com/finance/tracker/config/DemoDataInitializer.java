package com.finance.tracker.config;

import com.finance.tracker.model.Category;
import com.finance.tracker.model.CategoryType;
import com.finance.tracker.model.Role;
import com.finance.tracker.model.User;
import com.finance.tracker.repository.CategoryRepository;
import com.finance.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@Profile("local")
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true")
public class DemoDataInitializer implements ApplicationRunner {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
    private final String userPassword;

    public DemoDataInitializer(UserRepository userRepository, CategoryRepository categoryRepository,
                               PasswordEncoder passwordEncoder,
                               @Value("${DEMO_ADMIN_PASSWORD:}") String adminPassword,
                               @Value("${DEMO_USER_PASSWORD:}") String userPassword) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
        this.userPassword = userPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminPassword.isBlank() || userPassword.isBlank()) {
            throw new IllegalStateException("Local demo passwords are missing from the ignored environment file");
        }
        ensureUser("admin", "admin@example.com", adminPassword, Set.of(Role.USER, Role.ADMIN));
        User demoUser = ensureUser("demo", "demo@example.com", userPassword, Set.of(Role.USER));
        addCategory(demoUser, "Salary", CategoryType.INCOME);
        addCategory(demoUser, "Groceries", CategoryType.EXPENSE);
        addCategory(demoUser, "Housing", CategoryType.EXPENSE);
    }

    private User ensureUser(String username, String email, String rawPassword, Set<Role> roles) {
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() ->
                userRepository.save(new User(username, email, passwordEncoder.encode(rawPassword), roles)));
    }

    private void addCategory(User user, String name, CategoryType type) {
        if (!categoryRepository.existsByUser_IdAndNameIgnoreCaseAndDeletedAtIsNull(user.getId(), name)) {
            categoryRepository.save(new Category(name, type, user));
        }
    }
}
