package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository("userDbStorage")
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User create(User user) {
        SimpleJdbcInsert simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("users")
                .usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("email", user.getEmail());
        parameters.put("login", user.getLogin());
        parameters.put("name", user.getName());
        parameters.put("birthday", user.getBirthday());

        Long id = simpleJdbcInsert.executeAndReturnKey(parameters).longValue();
        user.setId(id);

        if (user.getFriends() != null && !user.getFriends().isEmpty()) {
            saveFriends(user);
        }

        log.info("Пользователь создан в БД: id = {}, login = {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(),
                user.getBirthday(), user.getId());

        if (user.getFriends() != null) {
            String deleteFriendsSql = "DELETE FROM friendships WHERE user_id = ?";
            jdbcTemplate.update(deleteFriendsSql, user.getId());

            if (!user.getFriends().isEmpty()) {
                saveFriends(user);
            }
        }

        log.info("Пользователь обновлен в БД: id = {}, login = {}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT * FROM users";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper());

        for (User user : users) {
            user.setFriends(getUserFriends(user.getId()));
        }

        return users;
    }

    @Override
    public Optional<User> getById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, new UserRowMapper(), id);

        if (users.isEmpty()) {
            return Optional.empty();
        }

        User user = users.get(0);
        user.setFriends(getUserFriends(user.getId()));

        return Optional.of(user);
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
        log.info("Пользователь удален из БД: id = {}", id);
    }

    @Override
    public boolean contains(Long id) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    private void saveFriends(User user) {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, ?)";

        for (Map.Entry<Long, FriendshipStatus> entry : user.getFriends().entrySet()) {
            jdbcTemplate.update(sql, user.getId(), entry.getKey(), entry.getValue().name());
        }
    }

    private Map<Long, FriendshipStatus> getUserFriends(Long userId) {
        String sql = "SELECT friend_id, status FROM friendships WHERE user_id = ?";

        Map<Long, FriendshipStatus> friends = new HashMap<>();
        jdbcTemplate.query(sql, (rs) -> {
            Long friendId = rs.getLong("friend_id");
            String status = rs.getString("status");
            friends.put(friendId, FriendshipStatus.valueOf(status));
        }, userId);

        return friends;
    }

    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }
    }
}