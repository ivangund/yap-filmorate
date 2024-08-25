package ru.yandex.practicum.filmorate.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.ErrorResponse;
import ru.yandex.practicum.filmorate.model.Film;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int idCounter = 1;
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @PostMapping
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        validateReleaseDate(film.getReleaseDate());

        film.setId(idCounter++);
        films.put(film.getId(), film);
        log.info("Фильм {} успешно добавлен (ID {})", film.getName(), film.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(film);
    }

    @PutMapping
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film) {
        validateReleaseDate(film.getReleaseDate());

        if (films.containsKey(film.getId())) {
            films.put(film.getId(), film);
            log.info("Фильм {} успешно обновлен (ID {})", film.getName(), film.getId());

            return ResponseEntity.ok(film);
        }

        String errorMessage =
                "Не удалось обновить фильм с ID " + film.getId() + ", так как он не найден";
        log.warn(errorMessage);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(errorMessage));
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return new ArrayList<>(films.values());
    }

    private void validateReleaseDate(LocalDate releaseDate) {
        if (releaseDate.isBefore(EARLIEST_RELEASE_DATE)) {
            throw new ValidationException("Дата релиза не может быть раньше 28.12.1895");
        }
    }
}
