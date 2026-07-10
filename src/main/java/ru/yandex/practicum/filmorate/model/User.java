package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    long id;
    @Email(message = "Email должен быть корректным")
    String email;
    String login;
    String name;
    LocalDate birthday;
}
