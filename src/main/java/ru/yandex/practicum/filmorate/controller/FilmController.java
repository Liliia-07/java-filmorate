package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION = 200;

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getAllFilm() {
        log.info("Получен запрос на получение всех фильмов. Текущее количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        validateFilm(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм успешно создан с id = {}: {}", film.getId(), film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Получен запрос на обновление фильма с id = {}", film.getId());
        if (!films.containsKey(film.getId())) {
            log.error("Ошибка обновления: фильм с id = {} не найден", film.getId());
            throw new ValidationException("Фильм с id = " + film.getId() + " не найден");
        }
        validateFilm(film);

        films.put(film.getId(), film);
        log.info("Фильм с id = {} успешно обновлен: {}", film.getId(), film);
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Ошибка валидации: пустое название фильма");
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription().length() > MAX_DESCRIPTION) {
            log.error("Ошибка валидации: превышена длина описания ({} символов из {})",
                    film.getDescription().length(), MAX_DESCRIPTION);
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            log.error("Ошибка валидации: дата релиза {} раньше 28 декабря 1895 года", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            log.error("Ошибка валидации: продолжительность фильма должна быть положительной: {}",
                    film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}