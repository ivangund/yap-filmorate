package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import java.util.*;

@Service
public class UserService {

    private final UserStorage userStorage;
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        if (userStorage.getUserById(user.getId()) == null) {
            throw new NotFoundException(
                    "Пользователь с идентификатором " + user.getId() + " не найден.");
        }
        return userStorage.updateUser(user);
    }

    public User getUserById(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с идентификатором " + id + " не найден.");
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);

        friends.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friends.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    public void removeFriend(int userId, int friendId) {
        getUserById(userId);
        getUserById(friendId);

        Set<Integer> userFriends = friends.get(userId);
        Set<Integer> friendFriends = friends.get(friendId);

        if (userFriends != null && userFriends.remove(friendId)) {
            if (friendFriends != null) {
                friendFriends.remove(userId);
            }
        } else {
            throw new NotFoundException(
                    "Пользователь с ID " + friendId + " не является другом пользователя с ID "
                            + userId + ".");
        }
    }

    public List<User> getFriends(int userId) {
        getUserById(userId);

        return friends.getOrDefault(userId, Collections.emptySet()).stream()
                .map(this::getUserById)
                .toList();
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        getUserById(userId);
        getUserById(otherId);

        Set<Integer> userFriends = friends.getOrDefault(userId, Collections.emptySet());
        Set<Integer> otherFriends = friends.getOrDefault(otherId, Collections.emptySet());

        Set<Integer> commonFriends = new HashSet<>(userFriends);
        commonFriends.retainAll(otherFriends);

        return commonFriends.stream()
                .map(this::getUserById)
                .toList();
    }
}
