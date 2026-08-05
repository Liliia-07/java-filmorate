package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class MpaController {

    private final FilmService filmService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Mpa> getAllMpas() {
        log.info("GET /mpa - Получение всех рейтингов MPA");
        return filmService.getAllMpas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mpa> getMpaById(@PathVariable Long id) {
        log.info("GET /mpa/{} - Получение рейтинга MPA по id", id);
        try {
            Mpa mpa = filmService.getMpaById(id);
            return ResponseEntity.ok(mpa);
        } catch (NotFoundException e) {
            throw e;  // Будет обработано GlobalExceptionHandler
        }
    }
}