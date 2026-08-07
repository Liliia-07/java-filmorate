package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;

public class MpaMapper {

    public static MpaDto toDto(Mpa mpa) {
        if (mpa == null) {
            return null;
        }
        MpaDto dto = new MpaDto();
        dto.setId(mpa.getId());
        dto.setName(mpa.getName());
        return dto;
    }

    public static Mpa toEntity(MpaDto dto) {
        if (dto == null) {
            return null;
        }
        Mpa mpa = new Mpa();
        mpa.setId(dto.getId());
        mpa.setName(dto.getName());
        return mpa;
    }

    public static List<MpaDto> toDtoList(List<Mpa> mpas) {
        if (mpas == null) {
            return List.of();
        }
        return mpas.stream()
                .map(MpaMapper::toDto)
                .toList();
    }
}