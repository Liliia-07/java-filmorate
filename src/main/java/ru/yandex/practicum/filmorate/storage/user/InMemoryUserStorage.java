package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь создан в хранилище: id = {}, login = {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        log.info("Пользователь обновлен в хранилище: id = {}, login = {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public List<User> getAll() {
        log.info("Получены все пользователи из хранилища. Количество: {}", users.size());
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void delete(Long id) {
        User removed = users.remove(id);
        if (removed != null) {
            log.info("Пользователь удален из хранилища: id = {}, login = {}", id, removed.getLogin());
        } else {
            log.warn("Попытка удалить несуществующего пользователя с id = {}", id);
        }
    }

    @Override
    public boolean contains(Long id) {
        return users.containsKey(id);
    }

    private long getNextId() {
        return idCounter++;
    }
}