package org.ashroev.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegistrationRequest {

    @NotBlank(message = "Укажите имя пользователя")
    @Size(min = 3, max = 50, message = "Имя пользователя должно быть от 3 до 50 символов")
    private String username;

    @NotBlank(message = "Укажите email")
    @Email(message = "Введите корректный email")
    private String email;

    @NotBlank(message = "Укажите пароль")
    @Size(min = 6, max = 100, message = "Пароль должен быть не короче 6 символов")
    private String password;
}
