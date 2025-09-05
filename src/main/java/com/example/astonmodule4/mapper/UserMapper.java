package com.example.astonmodule4.mapper;

import com.example.astonmodule4.model.User;
import com.example.astonmodule4.model.dto.request.CreateUserRequest;
import com.example.astonmodule4.model.dto.request.UpdateUserRequest;
import com.example.astonmodule4.model.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public class UserMapper {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(formatDateTime(user.getCreated_at()))
                .build();
    }

    public User fromCreateRequest(CreateUserRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .created_at(LocalDateTime.now())
                .build();
    }

    public void updateFromRequest(UpdateUserRequest request, User user) {
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(FORMATTER) : null;
    }
}
