package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public class GenreMapper {

    public static GenreDto toDto(Genre genre) {
        if (genre == null) {
            return null;
        }
        GenreDto dto = new GenreDto();
        dto.setId(genre.getId());
        dto.setName(genre.getName());
        return dto;
    }

    public static Genre toEntity(GenreDto dto) {
        if (dto == null) {
            return null;
        }
        Genre genre = new Genre();
        genre.setId(dto.getId());
        genre.setName(dto.getName());
        return genre;
    }

    public static List<GenreDto> toDtoList(List<Genre> genres) {
        if (genres == null) {
            return List.of();
        }
        return genres.stream()
                .map(GenreMapper::toDto)
                .toList();
    }
}