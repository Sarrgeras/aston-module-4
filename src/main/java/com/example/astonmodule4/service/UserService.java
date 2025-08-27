package com.example.astonmodule4.service;

import com.example.astonmodule4.exception.UserAlreadyExistsException;
import com.example.astonmodule4.exception.UserNotFoundException;
import com.example.astonmodule4.mapper.UserMapper;
import com.example.astonmodule4.model.User;
import com.example.astonmodule4.model.dto.request.CreateUserRequest;
import com.example.astonmodule4.model.dto.request.UpdateUserRequest;
import com.example.astonmodule4.model.dto.response.UserResponse;
import com.example.astonmodule4.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse createUser(CreateUserRequest userRequest) {
        log.info("Creating user with email: {}", userRequest.getEmail());

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("User with email {} already exists", userRequest.getEmail());
            throw new UserAlreadyExistsException(userRequest.getEmail());
        }

        User user = userMapper.fromCreateRequest(userRequest);
        User savedUser = userRepository.save(user);

        log.info("User created successfully with ID: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }
    public UserResponse getUserById(Long id) {
        log.debug("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new UserNotFoundException(id);
                });

        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest userRequest) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found for update with ID: {}", id);
                    return new UserNotFoundException(id);
                });

        if (userRequest.getEmail() != null &&
                !userRequest.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userRequest.getEmail())) {
            log.warn("Email {} already exists", userRequest.getEmail());
            throw new UserAlreadyExistsException(userRequest.getEmail());
        }

        userMapper.updateFromRequest(userRequest, user);
        User updatedUser = userRepository.save(user);

        log.info("User with ID: {} updated successfully", id);
        return userMapper.toResponse(updatedUser);
    }

    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            log.error("User not found for deletion with ID: {}", id);
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
        log.info("User with ID: {} deleted successfully", id);
    }
}
