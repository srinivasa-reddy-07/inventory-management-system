package com.inventorymanagement.ims;

import com.inventorymanagement.ims.user.User;
import com.inventorymanagement.ims.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class ImsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImsApplication.class, args);
    }

    // This bean runs on startup and creates a default admin user if one doesn't exist
    // In ImsApplication.java
    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create admin user if not exists
            userRepository.findByUsername("admin").or(() -> {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("password"));
                admin.setRoles("ROLE_ADMIN"); // Spring Security expects roles to start with ROLE_
                return java.util.Optional.of(userRepository.save(admin));
            });

            // Create regular user if not exists
            userRepository.findByUsername("user").or(() -> {
                User regularUser = new User();
                regularUser.setUsername("user");
                regularUser.setPassword(passwordEncoder.encode("password"));
                regularUser.setRoles("ROLE_USER");
                return java.util.Optional.of(userRepository.save(regularUser));
            });
        };
    }
}