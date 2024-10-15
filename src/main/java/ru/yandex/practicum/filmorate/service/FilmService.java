package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import java.util.*;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final Map<Integer, Set<Integer>> likes = new HashMap<>();

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        if (filmStorage.getFilmById(film.getId()) == null) {
            throw new NotFoundException("Фильм с идентификатором " + film.getId() + " не найден.");
        }
        return filmStorage.updateFilm(film);
    }

    public Film getFilmById(int id) {
        Film film = filmStorage.getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Фильм с идентификатором " + id + " не найден.");
        }
        return film;
    }

    public List<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(int filmId, int userId) {
        getFilmById(filmId);
        userService.getUserById(userId);

        likes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    public void removeLike(int filmId, int userId) {
        getFilmById(filmId);
        userService.getUserById(userId);

        Set<Integer> filmLikes = likes.get(filmId);
        if (filmLikes == null || !filmLikes.remove(userId)) {
            throw new NotFoundException(
                    "Лайк от пользователя с ID " + userId + " для фильма с ID " + filmId
                            + " не найден.");
        }
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted((f1, f2) -> {
                    int likesFirst = likes.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    int likesSecond = likes.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Integer.compare(likesSecond, likesFirst);
                })
                .limit(count)
                .toList();
    }
}
