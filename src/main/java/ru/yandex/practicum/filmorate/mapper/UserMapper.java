package ru.yandex.practicum.filmorate.mapper;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setLogin(user.getLogin());
        dto.setName(user.getName());
        dto.setBirthday(user.getBirthday());

        Map<Long, String> friendsDto = new HashMap<>();
        if (user.getFriends() != null) {
            for (Map.Entry<Long, FriendshipStatus> entry : user.getFriends().entrySet()) {
                friendsDto.put(entry.getKey(), entry.getValue().name());
            }
        }
        dto.setFriends(friendsDto);

        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setId(dto.getId());
        user.setEmail(dto.getEmail());
        user.setLogin(dto.getLogin());
        user.setName(dto.getName());
        user.setBirthday(dto.getBirthday());

        Map<Long, FriendshipStatus> friendsEntity = new HashMap<>();
        if (dto.getFriends() != null) {
            for (Map.Entry<Long, String> entry : dto.getFriends().entrySet()) {
                friendsEntity.put(entry.getKey(), FriendshipStatus.valueOf(entry.getValue()));
            }
        }
        user.setFriends(friendsEntity);

        return user;
    }

    public static List<UserDto> toDtoList(List<User> users) {
        if (users == null) {
            return List.of();
        }
        return users.stream()
                .map(UserMapper::toDto)
                .toList();
    }
}