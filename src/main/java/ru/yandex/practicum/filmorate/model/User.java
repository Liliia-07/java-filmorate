package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    private long id;
    @Email(message = "Email должен быть корректным")
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
    private Map<Long, FriendshipStatus> friends = new HashMap<>();
}
