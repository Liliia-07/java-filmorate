package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private FilmController filmController;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
        validFilm = new Film();
        validFilm.setName("Test Film");
        validFilm.setDescription("Test Description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void shouldCreateFilmWithValidData() {
        Film createdFilm = filmController.createFilm(validFilm);
        assertNotNull(createdFilm);
        assertNotNull(createdFilm.getId());
        assertEquals("Test Film", createdFilm.getName());
    }

    @Test
    void shouldNotCreateFilmWithEmptyName() {
        validFilm.setName("");
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void shouldNotCreateFilmWithNullName() {
        validFilm.setName(null);
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void shouldNotCreateFilmWithDescriptionExceedingMaxLength() {
        validFilm.setDescription("A".repeat(201));
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void shouldCreateFilmWithDescriptionExactlyMaxLength() {
        validFilm.setDescription("A".repeat(200));
        Film createdFilm = filmController.createFilm(validFilm);
        assertEquals(200, createdFilm.getDescription().length());
    }

    @Test
    void shouldNotCreateFilmWithReleaseDateBeforeCinemaBirthday() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void shouldCreateFilmWithReleaseDateOnCinemaBirthday() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        Film createdFilm = filmController.createFilm(validFilm);
        assertEquals(LocalDate.of(1895, 12, 28), createdFilm.getReleaseDate());
    }

    @Test
    void shouldNotCreateFilmWithNegativeDuration() {
        validFilm.setDuration(-1);
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void shouldNotCreateFilmWithZeroDuration() {
        validFilm.setDuration(0);
        assertThrows(ValidationException.class, () -> filmController.createFilm(validFilm));
    }

    @Test
    void shouldCreateFilmWithPositiveDuration() {
        validFilm.setDuration(1);
        Film createdFilm = filmController.createFilm(validFilm);
        assertEquals(1, createdFilm.getDuration());
    }

    @Test
    void shouldUpdateExistingFilm() {
        Film createdFilm = filmController.createFilm(validFilm);
        createdFilm.setName("Updated Film");
        Film updatedFilm = filmController.updateFilm(createdFilm);
        assertEquals("Updated Film", updatedFilm.getName());
    }

    @Test
    void shouldNotUpdateNonExistentFilm() {
        validFilm.setId(999L);
        assertThrows(ValidationException.class, () -> filmController.updateFilm(validFilm));
    }

    @Test
    void shouldReturnAllFilms() {
        filmController.createFilm(validFilm);
        assertEquals(1, filmController.getAllFilm().size());
    }

    @Test
    void shouldHandleMultipleFilms() {
        filmController.createFilm(validFilm);

        Film secondFilm = new Film();
        secondFilm.setName("Second Film");
        secondFilm.setDescription("Second Description");
        secondFilm.setReleaseDate(LocalDate.of(2020, 1, 1));
        secondFilm.setDuration(90);

        filmController.createFilm(secondFilm);
        assertEquals(2, filmController.getAllFilm().size());
    }
}