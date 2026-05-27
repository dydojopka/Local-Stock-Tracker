package org.ashroev.service;

import lombok.RequiredArgsConstructor;
import org.ashroev.dto.RegistrationRequest;
import org.ashroev.model.Role;
import org.ashroev.model.User;
import org.ashroev.repository.RoleRepository;
import org.ashroev.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ROLE_USER = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public void register(RegistrationRequest request) {
        Role userRole = roleRepository.findByName(ROLE_USER)
                .orElseGet(() -> roleRepository.save(new Role(ROLE_USER)));

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setEmail(request.getEmail().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(Set.of(userRole));

        userRepository.save(user);
    }
}
