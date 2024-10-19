package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class UserDbStorageTests {

    private final UserDbStorage userStorage;

    @Autowired
    public UserDbStorageTests(JdbcTemplate jdbcTemplate) {
        this.userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void testCreateUser() {
        User user = new User(0, "test@example.com", "user", "User", LocalDate.of(2001, 1, 1));
        User createdUser = userStorage.createUser(user);

        assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
        assertThat(createdUser.getLogin()).isEqualTo("user");
        assertThat(createdUser.getName()).isEqualTo("User");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(2001, 1, 1));
    }

    @Test
    void testUpdateUser() {
        User user = new User(0, "test@example.com", "user", "User", LocalDate.of(2001, 1, 1));
        User createdUser = userStorage.createUser(user);

        assertThat(createdUser.getName()).isEqualTo("User");

        createdUser.setName("Updated User");
        User updatedUser = userStorage.updateUser(createdUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated User");
    }

    @Test
    void testGetUserById() {
        User user = new User(0, "test@example.com", "user", "User", LocalDate.of(2001, 1, 1));
        User createdUser = userStorage.createUser(user);

        User retrievedUser = userStorage.getUserById(createdUser.getId());

        assertThat(retrievedUser)
                .hasFieldOrPropertyWithValue("id", createdUser.getId())
                .hasFieldOrPropertyWithValue("email", createdUser.getEmail())
                .hasFieldOrPropertyWithValue("login", createdUser.getLogin())
                .hasFieldOrPropertyWithValue("name", createdUser.getName())
                .hasFieldOrPropertyWithValue("birthday", createdUser.getBirthday());
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User(0, "test1@example.com", "user1", "User1", LocalDate.of(2000, 1, 1));
        User user2 = new User(0, "test2@example.com", "user2", "User2", LocalDate.of(2001, 1, 1));

        userStorage.createUser(user1);
        userStorage.createUser(user2);

        List<User> users = userStorage.getAllUsers();

        assertThat(users).hasSize(2);
    }

    @Test
    void testAddAndRemoveFriend() {
        User user1 = userStorage.createUser(
                new User(0, "test1@example.com", "user1", "User1", LocalDate.of(2000, 1, 1)));
        User user2 = userStorage.createUser(
                new User(0, "test2@example.com", "user2", "User2", LocalDate.of(2001, 1, 1)));

        userStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userStorage.getFriends(user1.getId());
        assertThat(friends).hasSize(1).contains(user2);

        userStorage.removeFriend(user1.getId(), user2.getId());

        List<User> updatedFriends = userStorage.getFriends(user1.getId());
        assertThat(updatedFriends).isEmpty();
    }

    @Test
    void testGetUserByIdNotFound() {
        assertThatThrownBy(() -> userStorage.getUserById(999))
                .isInstanceOf(NotFoundException.class);
    }
}
