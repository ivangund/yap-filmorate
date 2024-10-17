package ru.yandex.practicum.filmorate.service;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.util.*;

@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        userStorage.getUserById(user.getId());
        return userStorage.updateUser(user);
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);

        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);

        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        getUserById(userId);

        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        getUserById(userId);
        getUserById(otherId);

        List<User> userFriends = userStorage.getFriends(userId);
        List<User> otherFriends = userStorage.getFriends(otherId);

        Set<Integer> userFriendIds = userFriends.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        Set<Integer> otherFriendIds = otherFriends.stream()
                .map(User::getId)
                .collect(Collectors.toSet());

        userFriendIds.retainAll(otherFriendIds);

        return userFriendIds.stream()
                .map(this::getUserById)
                .toList();
    }
}
