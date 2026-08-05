package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor
public class GenreController {

    private final FilmService filmService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Genre> getAllGenres() {
        log.info("GET /genres - Получение всех жанров");
        return filmService.getAllGenres();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Genre getGenreById(@PathVariable Long id) {
        log.info("GET /genres/{} - Получение жанра по id", id);
        return filmService.getGenreById(id);
    }
}