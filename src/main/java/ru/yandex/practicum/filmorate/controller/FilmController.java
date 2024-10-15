package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import lombok.extern.slf4j.Slf4j;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        Film addedFilm = filmService.addFilm(film);
        log.info("Фильм '{}' успешно добавлен.", addedFilm.getName());
        return ResponseEntity.status(201).body(addedFilm);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        Film updatedFilm = filmService.updateFilm(film);
        log.info("Фильм '{}' успешно обновлен.", updatedFilm.getName());
        return ResponseEntity.ok(updatedFilm);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilmById(@PathVariable @Positive int id) {
        Film film = filmService.getFilmById(id);
        return ResponseEntity.ok(film);
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        List<Film> films = filmService.getAllFilms();
        return ResponseEntity.ok(films);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(
            @PathVariable @Positive int id,
            @PathVariable @Positive int userId) {
        filmService.addLike(id, userId);
        log.info("Пользователь с ID {} поставил лайк фильму с ID {}", userId, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(
            @PathVariable @Positive int id,
            @PathVariable @Positive int userId) {
        filmService.removeLike(id, userId);
        log.info("Пользователь с ID {} удалил лайк у фильма с ID {}", userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getMostPopularFilms(
            @RequestParam(defaultValue = "10") @Positive int count) {
        List<Film> popularFilms = filmService.getMostPopularFilms(count);
        return ResponseEntity.ok(popularFilms);
    }
}
