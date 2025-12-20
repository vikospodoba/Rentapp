package com.example.rentapp.service;

import com.example.rentapp.dto.UserRegistrationDto;
import com.example.rentapp.entity.User;
import com.example.rentapp.repository.UserRepository;
import com.example.rentapp.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal())) {

            Object principal = authentication.getPrincipal();
            if (principal instanceof CustomUserDetails customUserDetails) {
                return customUserDetails.getUser();
            } else if (principal instanceof UserDetails userDetails) {
                return findByUsername(userDetails.getUsername()).orElse(null);
            } else if (principal instanceof String username) {
                return findByUsername(username).orElse(null);
            }
        }
        return null;
    }

    public User registerUser(UserRegistrationDto registrationDto) {
        if (existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEmail(registrationDto.getEmail());
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setPhone(registrationDto.getPhone());
        user.setRole("CLIENT");

        return userRepository.save(user);
    }

    public User save(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty() &&
                !user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("CLIENT");
        } else {
            user.setRole(user.getRole().toUpperCase());
        }
        return userRepository.save(user);
    }

    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<User> findByRole(String role) {
        return userRepository.findAll().stream()
                .filter(user -> role.equals(user.getRole()))
                .toList();
    }

    public long getTotalUsers() {
        return userRepository.count();
    }
}