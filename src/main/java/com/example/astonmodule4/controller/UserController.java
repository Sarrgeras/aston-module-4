package com.example.astonmodule4.controller;

import com.example.astonmodule4.model.dto.request.CreateUserRequest;
import com.example.astonmodule4.model.dto.request.UpdateUserRequest;
import com.example.astonmodule4.model.dto.response.UserResponse;
import com.example.astonmodule4.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @GetMapping
    public List<UserResponse> getAllUsers() {
        LOGGER.info("Getting all users process");
        List<UserResponse> users = userService.getAllUsers();
        LOGGER.debug("Getting count of users: {}", users.size());
        return users;
    }

    @PostMapping
    public UserResponse createUser(@RequestBody @Valid CreateUserRequest userRequest) {
        LOGGER.info("Creating user process: email={}", userRequest.getEmail());
        UserResponse createdUser = userService.createUser(userRequest);
        LOGGER.info("User created: ID={}, email={}", createdUser.getId(), createdUser.getEmail());
        return createdUser;
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable Long id,
                                   @RequestBody @Valid UpdateUserRequest userRequest) {
        LOGGER.info("Updating user process: id={}", id);
        UserResponse updatedUser = userService.updateUser(id, userRequest);
        LOGGER.info("User updated: ID={}, name={}, email={}",
                updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail());
        return updatedUser;
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        LOGGER.info("Getting user process: id={}", id);
        UserResponse gotUser = userService.getUserById(id);
        LOGGER.info("User got: ID={}, name={}, email={}",
                gotUser.getId(), gotUser.getName(), gotUser.getEmail());
        return gotUser;
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        LOGGER.info("Deleting user process: id={}", id);
        userService.deleteUser(id);
        LOGGER.info("User deleted: ID={}", id);
    }
}
