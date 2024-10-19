package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final FilmDbStorage filmDbStorage;

    @Autowired
    public MpaController(FilmDbStorage filmDbStorage) {
        this.filmDbStorage = filmDbStorage;
    }

    @GetMapping
    public List<MpaRating> getAllMpaRatings() {
        return filmDbStorage.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable int id) {
        return filmDbStorage.getMpaById(id);
    }
}
