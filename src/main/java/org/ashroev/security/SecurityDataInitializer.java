package org.ashroev.security;

import lombok.RequiredArgsConstructor;
import org.ashroev.model.Role;
import org.ashroev.model.User;
import org.ashroev.repository.RoleRepository;
import org.ashroev.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class SecurityDataInitializer implements CommandLineRunner {

    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        Role userRole = getOrCreateRole(ROLE_USER);
        Role adminRole = getOrCreateRole(ROLE_ADMIN);

        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@localstock.local");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(userRole, adminRole));
            userRepository.save(admin);
        }
    }

    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }
}
