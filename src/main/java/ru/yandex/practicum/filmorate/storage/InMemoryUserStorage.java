package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;
import java.util.*;

@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> userFriends = new HashMap<>();
    private int idCounter = 1;

    @Override
    public User createUser(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        userFriends.put(user.getId(), new HashSet<>());
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUserById(int id) {
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (users.containsKey(userId) && users.containsKey(friendId)) {
            userFriends.get(userId).add(friendId);
            userFriends.get(friendId).add(userId);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        if (userFriends.containsKey(userId)) {
            userFriends.get(userId).remove(friendId);
        }
        if (userFriends.containsKey(friendId)) {
            userFriends.get(friendId).remove(userId);
        }
    }

    @Override
    public List<User> getFriends(int userId) {
        Set<Integer> friendsIds = userFriends.getOrDefault(userId, Collections.emptySet());
        List<User> friends = new ArrayList<>();
        for (int friendId : friendsIds) {
            User friend = users.get(friendId);
            if (friend != null) {
                friends.add(friend);
            }
        }
        return friends;
    }
}
