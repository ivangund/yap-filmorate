package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("email@yandex.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(2001, 1, 1));
    }

    @Test
    public void testInvalidEmail() throws Exception {
        user.setEmail("incorrect-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testBlankLogin() throws Exception {
        user.setLogin("");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testLoginWithSpaces() throws Exception {
        user.setLogin("invalid login");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIncorrectBirthday() throws Exception {
        user.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testEmptyName() throws Exception {
        user.setName("");

        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setEmail(user.getEmail());
        savedUser.setLogin(user.getLogin());
        savedUser.setName(user.getLogin());
        savedUser.setBirthday(user.getBirthday());

        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.login").value(savedUser.getLogin()))
                .andExpect(jsonPath("$.name").value(savedUser.getName()))
                .andExpect(jsonPath("$.birthday").value(savedUser.getBirthday().toString()));
    }

    @Test
    public void testValidUser() throws Exception {
        User savedUser = new User();
        savedUser.setId(1);
        savedUser.setEmail(user.getEmail());
        savedUser.setLogin(user.getLogin());
        savedUser.setName(user.getName());
        savedUser.setBirthday(user.getBirthday());

        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedUser.getId()))
                .andExpect(jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(jsonPath("$.login").value(savedUser.getLogin()))
                .andExpect(jsonPath("$.name").value(savedUser.getName()))
                .andExpect(jsonPath("$.birthday").value(savedUser.getBirthday().toString()));
    }

    @Test
    public void testServiceThrowsValidationException() throws Exception {
        user.setEmail("incorrect-email");

        when(userService.createUser(any(User.class)))
                .thenThrow(new ValidationException("Некорректный формат email"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testAddFriend() throws Exception {
        int userId = 1;
        int friendId = 2;

        Mockito.doNothing().when(userService).addFriend(userId, friendId);

        mockMvc.perform(put("/users/{id}/friends/{friendId}", userId, friendId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testRemoveFriendNotAFriend() throws Exception {
        int userId = 1;
        int friendId = 2;

        Mockito.doThrow(new NotFoundException(
                        "Пользователь с ID " + friendId + " не является другом пользователя с ID "
                                + userId))
                .when(userService).removeFriend(userId, friendId);

        mockMvc.perform(delete("/users/{id}/friends/{friendId}", userId, friendId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetFriends() throws Exception {
        int userId = 1;
        User friend = new User();
        friend.setId(2);
        friend.setEmail("friend@yandex.ru");
        friend.setLogin("friendlogin");
        friend.setName("Friend");
        friend.setBirthday(LocalDate.of(2000, 5, 15));

        when(userService.getFriends(userId)).thenReturn(List.of(friend));

        mockMvc.perform(get("/users/{id}/friends", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(friend.getId()))
                .andExpect(jsonPath("$[0].email").value(friend.getEmail()))
                .andExpect(jsonPath("$[0].login").value(friend.getLogin()))
                .andExpect(jsonPath("$[0].name").value(friend.getName()))
                .andExpect(jsonPath("$[0].birthday").value(friend.getBirthday().toString()));
    }

    @Test
    public void testGetCommonFriends() throws Exception {
        int userId = 1;
        int otherId = 3;

        User commonFriend = new User();
        commonFriend.setId(2);
        commonFriend.setEmail("common@yandex.ru");
        commonFriend.setLogin("commonlogin");
        commonFriend.setName("CommonFriend");
        commonFriend.setBirthday(LocalDate.of(1999, 8, 20));

        when(userService.getCommonFriends(userId, otherId)).thenReturn(List.of(commonFriend));

        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", userId, otherId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(commonFriend.getId()))
                .andExpect(jsonPath("$[0].email").value(commonFriend.getEmail()))
                .andExpect(jsonPath("$[0].login").value(commonFriend.getLogin()))
                .andExpect(jsonPath("$[0].name").value(commonFriend.getName()))
                .andExpect(jsonPath("$[0].birthday").value(commonFriend.getBirthday().toString()));
    }
}
