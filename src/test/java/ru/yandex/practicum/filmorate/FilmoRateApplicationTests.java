package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
class FilmoRateApplicationTests {

    @Autowired
    private UserDbStorage userStorage;

    @Test
    void testCreateUser() {
        User user = createTestUser("create@test.com", "createuser", "Create User");

        User created = userStorage.create(user);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isPositive();
        assertThat(created.getEmail()).isEqualTo("create@test.com");
        assertThat(created.getLogin()).isEqualTo("createuser");
        assertThat(created.getName()).isEqualTo("Create User");
        assertThat(created.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    void testUpdateUser() {
        User user = createTestUser("update@test.com", "updateuser", "Original Name");
        User created = userStorage.create(user);

        created.setName("Updated Name");
        created.setEmail("updated@test.com");
        User updated = userStorage.update(created);

        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getEmail()).isEqualTo("updated@test.com");
    }

    @Test
    void testGetAllUsers() {
        User user1 = createTestUser("user1@test.com", "user1", "User One");
        User user2 = createTestUser("user2@test.com", "user2", "User Two");
        userStorage.create(user1);
        userStorage.create(user2);

        List<User> users = userStorage.getAll();

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    void testGetById() {
        User user = createTestUser("getbyid@test.com", "getbyid", "Get By ID");
        User created = userStorage.create(user);

        Optional<User> found = userStorage.getById(created.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(created.getId());
        assertThat(found.get().getEmail()).isEqualTo("getbyid@test.com");
    }

    @Test
    void testGetByIdNotFound() {
        Optional<User> found = userStorage.getById(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    void testDeleteUser() {
        User user = createTestUser("delete@test.com", "deleteuser", "Delete Me");
        User created = userStorage.create(user);

        userStorage.delete(created.getId());

        Optional<User> found = userStorage.getById(created.getId());
        assertThat(found).isEmpty();
    }

    @Test
    void testContains() {
        User user = createTestUser("contains@test.com", "contains", "Contains");
        User created = userStorage.create(user);

        assertThat(userStorage.contains(created.getId())).isTrue();
        assertThat(userStorage.contains(9999L)).isFalse();
    }

    @Test
    void testSaveAndGetFriends() {
        User user1 = createTestUser("friend1@test.com", "friend1", "Friend One");
        User user2 = createTestUser("friend2@test.com", "friend2", "Friend Two");

        User created1 = userStorage.create(user1);
        User created2 = userStorage.create(user2);

        created1.getFriends().put(created2.getId(), FriendshipStatus.CONFIRMED);
        userStorage.update(created1);

        User updated = userStorage.getById(created1.getId()).get();

        assertThat(updated.getFriends()).isNotEmpty();
        assertThat(updated.getFriends()).containsKey(created2.getId());
        assertThat(updated.getFriends().get(created2.getId())).isEqualTo(FriendshipStatus.CONFIRMED);
    }

    @Test
    void testFriendshipStatusUnconfirmed() {
        User user1 = createTestUser("unconfirmed1@test.com", "unconfirmed1", "User 1");
        User user2 = createTestUser("unconfirmed2@test.com", "unconfirmed2", "User 2");

        User created1 = userStorage.create(user1);
        User created2 = userStorage.create(user2);

        created1.getFriends().put(created2.getId(), FriendshipStatus.UNCONFIRMED);
        userStorage.update(created1);

        User updated = userStorage.getById(created1.getId()).get();

        assertThat(updated.getFriends().get(created2.getId())).isEqualTo(FriendshipStatus.UNCONFIRMED);
    }

    @Test
    void testDeleteFriendship() {
        User user1 = createTestUser("delfriend1@test.com", "delfriend1", "User 1");
        User user2 = createTestUser("delfriend2@test.com", "delfriend2", "User 2");

        User created1 = userStorage.create(user1);
        User created2 = userStorage.create(user2);

        created1.getFriends().put(created2.getId(), FriendshipStatus.CONFIRMED);
        userStorage.update(created1);

        created1.getFriends().remove(created2.getId());
        userStorage.update(created1);

        User updated = userStorage.getById(created1.getId()).get();
        assertThat(updated.getFriends()).isEmpty();
    }

    private User createTestUser(String email, String login, String name) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }
}