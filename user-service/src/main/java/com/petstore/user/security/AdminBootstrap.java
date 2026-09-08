package com.petstore.user.security;

import com.petstore.user.entity.Role;
import com.petstore.user.entity.User;
import com.petstore.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.default-email:admin@petstore.com}")
    private String defaultAdminEmail;

    @Value("${app.admin.default-password:AdminPassword123!}")
    private String defaultAdminPassword;

    @Value("${app.admin.default-name:Store Admin}")
    private String defaultAdminName;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        User admin = userRepository.findByEmail(defaultAdminEmail).orElse(null);
        if (admin == null) {
            logger.info("No ADMIN account detected in database. Bootstrapping default administrator: {}", defaultAdminEmail);
            admin = new User();
            admin.setName(defaultAdminName);
            admin.setEmail(defaultAdminEmail);
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setPhone("9876543210");
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            logger.info("Default administrator created successfully with role ADMIN.");
        } else {
            admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
            logger.info("Admin account {} verified and password synchronized.", defaultAdminEmail);
        }

        // Also synchronize customer password if present
        userRepository.findByEmail("customer@petstore.com").ifPresent(customer -> {
            customer.setPassword(passwordEncoder.encode("Customer123!"));
            userRepository.save(customer);
        });
    }
}
