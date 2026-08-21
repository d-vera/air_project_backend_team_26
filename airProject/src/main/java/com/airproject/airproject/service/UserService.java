package com.airproject.airproject.service;

import com.airproject.airproject.dto.AssignRoleRequest;
import com.airproject.airproject.dto.UpdateUserRequest;
import com.airproject.airproject.dto.UserResponse;
import com.airproject.airproject.exception.UserNotFoundException;
import com.airproject.airproject.model.User;
import com.airproject.airproject.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateCurrentUser(String email, UpdateUserRequest request) {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }



    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        return UserResponse.fromEntity(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        if (StringUtils.hasText(request.getFirstName())) {
            user.setFirstName(request.getFirstName());
        }
        if (StringUtils.hasText(request.getLastName())) {
            user.setLastName(request.getLastName());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        user.setActive(false);
        userRepository.save(user);
    }

    public UserResponse assignRole(Long id, AssignRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        user.setRole(request.getRole());
        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }
}
