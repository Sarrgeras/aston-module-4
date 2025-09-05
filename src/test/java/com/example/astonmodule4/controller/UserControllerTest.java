package com.example.astonmodule4.controller;

import com.example.astonmodule4.exception.UserAlreadyExistsException;
import com.example.astonmodule4.exception.UserNotFoundException;
import com.example.astonmodule4.model.dto.request.CreateUserRequest;
import com.example.astonmodule4.model.dto.request.UpdateUserRequest;
import com.example.astonmodule4.model.dto.response.UserResponse;
import com.example.astonmodule4.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserControllerTest.TestConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    private final UserResponse testUser = UserResponse.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .createdAt(LocalDateTime.now().toString())
            .build();

    @Test
    void getAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(
                testUser,
                UserResponse.builder()
                        .id(2L)
                        .name("Another User")
                        .email("another@example.com")
                        .createdAt(LocalDateTime.now().toString())
                        .build()
        ));

        mockMvc.perform(get("/api/users")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users", hasSize(2)))
                .andExpect(jsonPath("$._embedded.users[0].id", is(1)))
                .andExpect(jsonPath("$._embedded.users[0].name", is("Test User")))
                .andExpect(jsonPath("$._embedded.users[1].id", is(2)))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.create.href", notNullValue()));
    }

    @Test
    void createUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest("New User", "new@example.com");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.update.href", notNullValue()))
                .andExpect(jsonPath("$._links.delete.href", notNullValue()))
                .andExpect(jsonPath("$._links.users.href", notNullValue()));
    }

    @Test
    void getUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(testUser);

        mockMvc.perform(get("/api/users/1")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Test User")))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.update.href", notNullValue()))
                .andExpect(jsonPath("$._links.delete.href", notNullValue()))
                .andExpect(jsonPath("$._links.users.href", notNullValue()));
    }

    @Test
    void updateUser() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Updated Name", "updated@example.com");
        UserResponse updatedUser = UserResponse.builder()
                .id(1L)
                .name("Updated Name")
                .email("updated@example.com")
                .createdAt(LocalDateTime.now().toString())
                .build();

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaTypes.HAL_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Updated Name")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.delete.href", notNullValue()))
                .andExpect(jsonPath("$._links.users.href", notNullValue()));
    }

    @Test
    void deleteUser() throws Exception {
        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_UserNotFoundException() throws Exception {
        when(userService.getUserById(999L))
                .thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(get("/api/users/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")))
                .andExpect(jsonPath("$.message", containsString("999")));
    }

    @Test
    void updateUser_UserNotFoundException() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Updated Name", "updated@example.com");

        when(userService.updateUser(eq(999L), any(UpdateUserRequest.class)))
                .thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")));
    }

    @Test
    void deleteUser_UserNotFoundException() throws Exception {
        doThrow(new UserNotFoundException(999L))
                .when(userService).deleteUser(999L);

        mockMvc.perform(delete("/api/users/999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("NOT_FOUND")));
    }

    @Test
    void createUser_UserAlreadyExistsException() throws Exception {
        CreateUserRequest request = new CreateUserRequest("Test User", "existing@example.com");

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("existing@example.com"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")))
                .andExpect(jsonPath("$.message", containsString("existing@example.com")));
    }

    @Test
    void updateUser_UserAlreadyExistsException() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest("Updated Name", "existing@example.com");

        when(userService.updateUser(eq(1L), any(UpdateUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("existing@example.com"));

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code", is("CONFLICT")));
    }

    @Test
    void getAllUsers_EmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.users", hasSize(0)))
                .andExpect(jsonPath("$._links.self.href", notNullValue()))
                .andExpect(jsonPath("$._links.create.href", notNullValue()));
    }
}