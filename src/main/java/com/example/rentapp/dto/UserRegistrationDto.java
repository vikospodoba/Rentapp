package com.example.rentapp.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDto {

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 1, max = 50, message = "Имя пользователя должно содержать от 1 до 50 символов")
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 1, message = "Пароль должен содержать хотя бы 1 символ")
    private String password;

    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    private String email;

    private String firstName;
    private String lastName;
    private String phone;

}