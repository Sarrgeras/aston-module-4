package com.example.astonmodule4.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @Size(min = 2, max = 50, message = "Имя должно быть от 2 символов")
    private String name;

    @Email(message = "Некорректный формат email")
    private String email;
}
