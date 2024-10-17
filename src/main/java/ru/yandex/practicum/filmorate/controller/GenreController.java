package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final FilmDbStorage filmDbStorage;

    @Autowired
    public GenreController(FilmDbStorage filmDbStorage) {
        this.filmDbStorage = filmDbStorage;
    }

    @GetMapping
    public List<Genre> getAllGenres() {
        return filmDbStorage.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable int id) {
        return filmDbStorage.getGenreById(id);
    }
}
