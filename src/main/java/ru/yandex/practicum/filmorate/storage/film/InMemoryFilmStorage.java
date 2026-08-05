package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long idCounter = 1;

    @Override
    public Film create(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм создан в хранилище: id = {}, name = {}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        log.info("Фильм обновлен в хранилище: id = {}, name = {}", film.getId(), film.getName());
        return film;
    }

    @Override
    public List<Film> getAll() {
        log.info("Получены все фильмы из хранилища. Количество: {}", films.size());
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> getById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void delete(Long id) {
        Film removed = films.remove(id);
        if (removed != null) {
            log.info("Фильм удален из хранилища: id = {}, name = {}", id, removed.getName());
        } else {
            log.warn("Попытка удалить несуществующий фильм с id = {}", id);
        }
    }

    @Override
    public boolean contains(Long id) {
        return films.containsKey(id);
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return Math.max(currentMaxId + 1, idCounter++);
    }
}