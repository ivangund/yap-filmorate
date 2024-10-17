package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class FilmDbStorageTests {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Autowired
    public FilmDbStorageTests(JdbcTemplate jdbcTemplate) {
        this.filmStorage = new FilmDbStorage(jdbcTemplate);
        this.userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void testAddFilm() {
        Film film = new Film(0, "Фильм", "Описание фильма",
                LocalDate.of(2000, 1, 1), 100,
                List.of(new Genre(1, "Комедия")), new MpaRating(1, "G"));

        Film createdFilm = filmStorage.addFilm(film);

        assertThat(createdFilm.getName()).isEqualTo("Фильм");
        assertThat(createdFilm.getGenres()).hasSize(1);
    }

    @Test
    void testUpdateFilm() {
        Film film = new Film(0, "Фильм", "Описание фильма",
                LocalDate.of(2000, 1, 1), 100,
                List.of(new Genre(1, "Комедия")), new MpaRating(2, "PG"));
        Film createdFilm = filmStorage.addFilm(film);

        createdFilm.setName("Фильм измененный");
        createdFilm.setDescription("Новое описание");
        createdFilm.setGenres(List.of(new Genre(2, "Драма")));

        Film updatedFilm = filmStorage.updateFilm(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Фильм измененный");
        assertThat(updatedFilm.getDescription()).isEqualTo("Новое описание");
        assertThat(updatedFilm.getGenres()).hasSize(1)
                .extracting(Genre::getName)
                .contains("Драма");
    }

    @Test
    void testGetFilmById() {
        Film film = new Film(0, "Фильм", "Описание фильма",
                LocalDate.of(2000, 1, 1), 100,
                List.of(new Genre(1, "Комедия")), new MpaRating(3, "PG-13"));
        Film createdFilm = filmStorage.addFilm(film);

        Film retrievedFilm = filmStorage.getFilmById(createdFilm.getId());

        assertThat(retrievedFilm)
                .hasFieldOrPropertyWithValue("name", "Фильм")
                .hasFieldOrPropertyWithValue("description", "Описание фильма");
        assertThat(retrievedFilm.getGenres()).hasSize(1)
                .extracting(Genre::getName)
                .contains("Комедия");
    }

    @Test
    void testGetAllFilms() {
        Film film1 = new Film(0, "Фильм 1", "Описание фильма 1",
                LocalDate.of(2000, 1, 1), 100,
                List.of(new Genre(2, "Драма")), new MpaRating(2, "PG"));
        Film film2 = new Film(0, "Фильм 2", "Описание фильма 2",
                LocalDate.of(2001, 1, 1), 101,
                List.of(new Genre(3, "Мультфильм")), new MpaRating(3, "PG-13"));

        filmStorage.addFilm(film1);
        filmStorage.addFilm(film2);

        List<Film> films = filmStorage.getAllFilms();

        assertThat(films).hasSize(2);
    }

    @Test
    void testAddAndRemoveLike() {
        User user = new User(0, "test@example.com", "user", "User",
                LocalDate.of(2000, 1, 1));
        int userId = userStorage.createUser(user).getId();

        Film film = new Film(0, "Фильм", "Описание фильма",
                LocalDate.of(2000, 1, 1), 100,
                List.of(new Genre(4, "Триллер")), new MpaRating(4, "R"));
        Film createdFilm = filmStorage.addFilm(film);

        int filmId = createdFilm.getId();

        filmStorage.addLike(filmId, userId);

        List<Film> popularFilms = filmStorage.getMostPopularFilms(1);
        assertThat(popularFilms).hasSize(1).extracting(Film::getName).contains("Фильм");
    }

    @Test
    void testGetFilmByIdNotFound() {
        assertThatThrownBy(() -> filmStorage.getFilmById(999))
                .isInstanceOf(NotFoundException.class);
    }
}
