package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {

    private UserController userController;
    private User validUser;

    @BeforeEach
    void setUp() {
        userController = new UserController();
        validUser = new User();
        validUser.setEmail("test@example.com");
        validUser.setLogin("testLogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldCreateUserWithValidData() {
        User createdUser = userController.createUser(validUser);
        assertNotNull(createdUser);
        assertNotNull(createdUser.getId());
        assertEquals("test@example.com", createdUser.getEmail());
    }

    @Test
    void shouldNotCreateUserWithEmptyEmail() {
        validUser.setEmail("");
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void shouldNotCreateUserWithNullEmail() {
        validUser.setEmail(null);
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void shouldNotCreateUserWithEmailWithoutAtSymbol() {
        validUser.setEmail("invalid-email");
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void shouldNotCreateUserWithEmptyLogin() {
        validUser.setLogin("");
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void shouldNotCreateUserWithNullLogin() {
        validUser.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void shouldNotCreateUserWithLoginContainingSpaces() {
        validUser.setLogin("test login");
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void shouldUseLoginAsNameIfNameIsEmpty() {
        validUser.setName("");
        User createdUser = userController.createUser(validUser);
        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void shouldUseLoginAsNameIfNameIsNull() {
        validUser.setName(null);
        User createdUser = userController.createUser(validUser);
        assertEquals("testLogin", createdUser.getName());
    }

    @Test
    void shouldNotCreateUserWithFutureBirthday() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.createUser(validUser));
    }

    @Test
    void shouldCreateUserWithTodayBirthday() {
        validUser.setBirthday(LocalDate.now());
        User createdUser = userController.createUser(validUser);
        assertNotNull(createdUser);
        assertEquals(LocalDate.now(), createdUser.getBirthday());
    }

    @Test
    void shouldUpdateExistingUser() {
        User createdUser = userController.createUser(validUser);
        createdUser.setEmail("updated@example.com");
        User updatedUser = userController.updateUser(createdUser);
        assertEquals("updated@example.com", updatedUser.getEmail());
    }

    @Test
    void shouldNotUpdateNonExistentUser() {
        validUser.setId(999L);
        assertThrows(ValidationException.class, () -> userController.updateUser(validUser));
    }

    @Test
    void shouldReturnAllUsers() {
        userController.createUser(validUser);
        assertEquals(1, userController.getAllUsers().size());
    }
}