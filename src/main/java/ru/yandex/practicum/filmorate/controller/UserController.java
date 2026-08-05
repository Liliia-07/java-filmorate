package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<User> getAllUsers() {
        log.info("GET /users - Получение всех пользователей");
        return userService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User getUserById(@PathVariable Long id) {
        log.info("GET /users/{} - Получение пользователя по id", id);
        return userService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@Valid @RequestBody User user) {
        log.info("POST /users - Создание пользователя: {}", user.getLogin());
        return userService.create(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@Valid @RequestBody User user) {
        log.info("PUT /users - Обновление пользователя с id = {}", user.getId());
        return userService.update(user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        log.info("DELETE /users/{} - Удаление пользователя", id);
        userService.delete(id);
    }

    // Добавление в друзья
    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("PUT /users/{}/friends/{} - Добавление в друзья", id, friendId);
        userService.addFriend(id, friendId);
        return Map.of("message", "Friend added successfully");
    }

    // Удаление из друзей
    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("DELETE /users/{}/friends/{} - Удаление из друзей", id, friendId);
        userService.removeFriend(id, friendId);
    }

    // Получение списка друзей
    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getFriends(@PathVariable Long id) {
        log.info("GET /users/{}/friends - Получение друзей пользователя", id);
        return userService.getFriends(id);
    }

    // Получение списка общих друзей
    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("GET /users/{}/friends/common/{} - Получение общих друзей", id, otherId);
        return userService.getCommonFriends(id, otherId);
    }
}