package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

public class FilmMapper {

    public static FilmDto toDto(Film film) {
        if (film == null) {
            return null;
        }
        FilmDto dto = new FilmDto();
        dto.setId(film.getId());
        dto.setName(film.getName());
        dto.setDescription(film.getDescription());
        dto.setReleaseDate(film.getReleaseDate());
        dto.setDuration(film.getDuration());
        dto.setLikes(film.getLikes());
        dto.setMpa(MpaMapper.toDto(film.getMpa()));

        if (film.getGenres() != null) {
            dto.setGenres(film.getGenres().stream()
                    .map(GenreMapper::toDto)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        } else {
            dto.setGenres(new LinkedHashSet<>());
        }

        return dto;
    }

    public static Film toEntity(FilmDto dto) {
        if (dto == null) {
            return null;
        }
        Film film = new Film();
        film.setId(dto.getId());
        film.setName(dto.getName());
        film.setDescription(dto.getDescription());
        film.setReleaseDate(dto.getReleaseDate());
        film.setDuration(dto.getDuration());

        if (dto.getLikes() != null) {
            film.setLikes(dto.getLikes());
        } else {
            film.setLikes(new HashSet<>());
        }

        film.setMpa(MpaMapper.toEntity(dto.getMpa()));

        if (dto.getGenres() != null) {
            film.setGenres(dto.getGenres().stream()
                    .map(GenreMapper::toEntity)
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
        } else {
            film.setGenres(new LinkedHashSet<>());
        }

        return film;
    }

    public static List<FilmDto> toDtoList(List<Film> films) {
        if (films == null) {
            return List.of();
        }
        return films.stream()
                .map(FilmMapper::toDto)
                .toList();
    }
}