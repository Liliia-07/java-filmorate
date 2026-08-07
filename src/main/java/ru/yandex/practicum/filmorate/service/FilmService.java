package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.GenreMapper;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
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

    public FilmDto create(FilmDto filmDto) {
        Film film = FilmMapper.toEntity(filmDto);
        validateFilm(film);
        Film created = filmStorage.create(film);
        return FilmMapper.toDto(created);
    }

    public FilmDto update(FilmDto filmDto) {
        Film film = FilmMapper.toEntity(filmDto);
        if (!filmStorage.contains(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        validateFilm(film);
        Film updated = filmStorage.update(film);
        return FilmMapper.toDto(updated);
    }

    public List<FilmDto> getAll() {
        return FilmMapper.toDtoList(filmStorage.getAll());
    }

    public FilmDto getById(Long id) {
        Film film = filmStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
        return FilmMapper.toDto(film);
    }

    public void delete(Long id) {
        if (!filmStorage.contains(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        filmStorage.delete(id);
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));

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
        Film film = filmStorage.getById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));

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

    public List<FilmDto> getPopularFilms(int count) {
        List<Film> films;
        if (filmStorage instanceof FilmDbStorage) {
            films = ((FilmDbStorage) filmStorage).getPopularFilms(count);
        } else {
            films = filmStorage.getAll().stream()
                    .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                    .limit(count)
                    .toList();
        }
        return FilmMapper.toDtoList(films);
    }

    public List<GenreDto> getAllGenres() {
        List<Genre> genres;
        if (filmStorage instanceof FilmDbStorage) {
            genres = ((FilmDbStorage) filmStorage).getAllGenres();
        } else {
            genres = List.of();
        }
        return GenreMapper.toDtoList(genres);
    }

    public GenreDto getGenreById(Long id) {
        Genre genre;
        if (filmStorage instanceof FilmDbStorage) {
            genre = ((FilmDbStorage) filmStorage).getGenreById(id);
        } else {
            genre = null;
        }

        if (genre == null) {
            throw new NotFoundException("Жанр с id = " + id + " не найден");
        }

        return GenreMapper.toDto(genre);
    }

    public List<MpaDto> getAllMpas() {
        List<Mpa> mpas;
        if (filmStorage instanceof FilmDbStorage) {
            mpas = ((FilmDbStorage) filmStorage).getAllMpas();
        } else {
            mpas = List.of();
        }
        return MpaMapper.toDtoList(mpas);
    }

    public MpaDto getMpaById(Long id) {
        Mpa mpa;
        if (filmStorage instanceof FilmDbStorage) {
            try {
                mpa = ((FilmDbStorage) filmStorage).getMpaById(id);
            } catch (NotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new NotFoundException("Рейтинг MPA с id = " + id + " не найден");
            }
        } else {
            mpa = null;
        }

        if (mpa == null) {
            throw new NotFoundException("Рейтинг MPA с id = " + id + " не найден");
        }

        return MpaMapper.toDto(mpa);
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