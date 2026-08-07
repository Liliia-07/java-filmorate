package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public UserDto create(UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        validateUser(user);
        User created = userStorage.create(user);
        return UserMapper.toDto(created);
    }

    public UserDto update(UserDto userDto) {
        User user = UserMapper.toEntity(userDto);
        if (!userStorage.contains(user.getId())) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        validateUser(user);

        User existingUser = userStorage.getById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + user.getId() + " не найден"));

        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            user.setFriends(existingUser.getFriends());
        }

        User updated = userStorage.update(user);
        return UserMapper.toDto(updated);
    }

    public List<UserDto> getAll() {
        return UserMapper.toDtoList(userStorage.getAll());
    }

    public UserDto getById(Long id) {
        User user = userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
        return UserMapper.toDto(user);
    }

    public void delete(Long id) {
        if (!userStorage.contains(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        userStorage.delete(id);
    }

    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        if (!userStorage.contains(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }

        user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
        userStorage.update(user);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        if (!userStorage.contains(friendId)) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден");
        }

        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалить себя из друзей");
        }

        if (!user.getFriends().containsKey(friendId)) {
            log.info("Пользователи {} и {} не являются друзьями", userId, friendId);
            return;
        }

        user.getFriends().remove(friendId);
        userStorage.update(user);

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<UserDto> getFriends(Long userId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));

        List<User> friends = user.getFriends().keySet().stream()
                .map(id -> userStorage.getById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден")))
                .collect(Collectors.toList());

        return UserMapper.toDtoList(friends);
    }

    public List<UserDto> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User otherUser = userStorage.getById(otherUserId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + otherUserId + " не найден"));

        Set<Long> userFriends = new HashSet<>(user.getFriends().keySet());
        Set<Long> otherUserFriends = new HashSet<>(otherUser.getFriends().keySet());

        userFriends.retainAll(otherUserFriends);

        List<User> commonFriends = userFriends.stream()
                .map(id -> userStorage.getById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден")))
                .collect(Collectors.toList());

        return UserMapper.toDtoList(commonFriends);
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Электронная почта не может быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта должна содержать символ @");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя не задано, используется логин: {}", user.getLogin());
        }

        if (user.getFriends() == null) {
            user.setFriends(new HashMap<>());
        }
    }
}