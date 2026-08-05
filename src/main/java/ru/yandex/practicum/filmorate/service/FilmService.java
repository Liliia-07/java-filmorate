package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        validateFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        if (!filmStorage.contains(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        validateFilm(film);
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getById(Long id) {
        return filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public void delete(Long id) {
        if (!filmStorage.contains(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        filmStorage.delete(id);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = getById(filmId);

        if (!userStorage.contains(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь уже поставил лайк этому фильму");
        }

        if (filmStorage instanceof FilmDbStorage) {
            ((FilmDbStorage) filmStorage).addLike(filmId, userId);
        } else {
            film.getLikes().add(userId);
        }

        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = getById(filmId);

        if (!userStorage.contains(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        if (!film.getLikes().contains(userId)) {
            throw new ValidationException("Пользователь не ставил лайк этому фильму");
        }

        if (filmStorage instanceof FilmDbStorage) {
            ((FilmDbStorage) filmStorage).removeLike(filmId, userId);
        } else {
            film.getLikes().remove(userId);
        }

        log.info("Пользователь {} удалил лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        if (filmStorage instanceof FilmDbStorage) {
            return ((FilmDbStorage) filmStorage).getPopularFilms(count);
        }

        return filmStorage.getAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .toList();
    }

    public List<Genre> getAllGenres() {
        if (filmStorage instanceof FilmDbStorage) {
            return ((FilmDbStorage) filmStorage).getAllGenres();
        }
        return List.of();
    }

    public Genre getGenreById(Long id) {
        if (filmStorage instanceof FilmDbStorage) {
            Genre genre = ((FilmDbStorage) filmStorage).getGenreById(id);
            if (genre == null) {
                throw new NotFoundException("Жанр с id = " + id + " не найден");
            }
            return genre;
        }
        throw new NotFoundException("Жанр с id = " + id + " не найден");
    }

    public List<Mpa> getAllMpas() {
        if (filmStorage instanceof FilmDbStorage) {
            return ((FilmDbStorage) filmStorage).getAllMpas();
        }
        return List.of();
    }

    public Mpa getMpaById(Long id) {
        if (filmStorage instanceof FilmDbStorage) {
            try {
                return ((FilmDbStorage) filmStorage).getMpaById(id);
            } catch (NotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new NotFoundException("Рейтинг MPA с id = " + id + " не найден");
            }
        }
        throw new NotFoundException("Рейтинг MPA с id = " + id + " не найден");
    }

    private void validateFilm(Film film) {

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new ValidationException("Максимальная длина описания — " + MAX_DESCRIPTION_LENGTH + " символов");
        }

        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("Рейтинг MPA не может быть null");
        }
        try {
            getMpaById(film.getMpa().getId());
        } catch (NotFoundException e) {
            throw new NotFoundException("Рейтинг MPA с id = " + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre genre : film.getGenres()) {
                if (genre.getId() == null) {
                    throw new ValidationException("ID жанра не может быть null");
                }
                try {
                    getGenreById(genre.getId());
                } catch (NotFoundException e) {
                    throw new NotFoundException("Жанр с id = " + genre.getId() + " не найден");
                }
            }
        }
    }
}