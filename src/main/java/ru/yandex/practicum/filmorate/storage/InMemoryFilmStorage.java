package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();
    private int idCounter = 1;

    @Override
    public Film addFilm(Film film) {
        film.setId(idCounter++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film getFilmById(int id) {
        return films.get(id);
    }

    @Override
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }
}
