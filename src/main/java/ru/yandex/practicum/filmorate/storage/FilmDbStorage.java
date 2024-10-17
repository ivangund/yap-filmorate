package ru.yandex.practicum.filmorate.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import java.util.*;

@Component("filmDbStorage")
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // Создание фильма
    @Override
    public Film addFilm(Film film) {
        validateMpaId(film.getMpa().getId());
        validateGenreIds(film.getGenres());

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql,
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        addGenresToFilm(film);

        return film;
    }

    // Обновление фильма
    @Override
    public Film updateFilm(Film film) {
        validateMpaId(film.getMpa().getId());
        validateGenreIds(film.getGenres());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException("Фильм с ID " + film.getId() + " не найден");
        }

        deleteGenresFromFilm(film.getId());
        addGenresToFilm(film);

        return film;
    }

    // Получение фильма по ID
    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        Film film;

        try {
            film = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowToFilm(rs), id);
            Objects.requireNonNull(film).setGenres(getGenresByFilmId(id));
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с ID " + id + " не найден");
        }

        return film;
    }

    // Получение всех фильмов
    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs));
        films.forEach(film -> film.setGenres(getGenresByFilmId(film.getId())));
        return films;
    }

    // Добавление лайка к фильму
    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    // Удаление лайка с фильма
    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    // Получение самых популярных фильмов (по количеству лайков)
    @Override
    public List<Film> getMostPopularFilms(int count) {
        String sql = """
                    SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                           COUNT(fl.user_id) AS likes_count
                    FROM films f
                    LEFT JOIN film_likes fl ON f.film_id = fl.film_id
                    GROUP BY f.film_id
                    ORDER BY likes_count DESC
                    LIMIT ?
                """;
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToFilm(rs), count);
        films.forEach(film -> film.setGenres(getGenresByFilmId(film.getId())));
        return films;
    }

    // Добавление жанра к фильму
    private void addGenresToFilm(Film film) {
        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        if (film.getGenres() != null) {
            Set<Integer> uniqueGenres = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                if (uniqueGenres.add(genre.getId())) {
                    jdbcTemplate.update(sql, film.getId(), genre.getId());
                }
            }
        }
    }

    // Удаление жанра с фильма
    private void deleteGenresFromFilm(int filmId) {
        String sql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }

    // Получение жанра по ID
    public Genre getGenreById(int genreId) {
        String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                Genre genre = new Genre();
                genre.setId(rs.getInt("genre_id"));
                genre.setName(rs.getString("name"));
                return genre;
            }, genreId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Жанр с ID " + genreId + " не найден");
        }
    }

    // Получение всех жанров
    public List<Genre> getAllGenres() {
        String sql = "SELECT genre_id, name FROM genres";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        });
    }

    // Получение жанров фильма по ID
    private List<Genre> getGenresByFilmId(int filmId) {
        String sql = """
                    SELECT g.genre_id, g.name
                    FROM genres g
                    JOIN film_genres fg ON g.genre_id = fg.genre_id
                    WHERE fg.film_id = ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId);
    }

    // Получение рейтинга MPA по ID
    public MpaRating getMpaById(int mpaId) {
        String sql = "SELECT mpa_id, name FROM mpa_ratings WHERE mpa_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                MpaRating mpa = new MpaRating();
                mpa.setId(rs.getInt("mpa_id"));
                mpa.setName(rs.getString("name"));
                return mpa;
            }, mpaId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг MPA с ID " + mpaId + " не найден");
        }
    }

    // Получение всех рейтингов MPA
    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT mpa_id, name FROM mpa_ratings";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_id"));
            mpa.setName(rs.getString("name"));
            return mpa;
        });
    }

    // Проверка на существование рейтинга MPA по ID
    private void validateMpaId(int mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE mpa_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpaId);
        if (count == null || count == 0) {
            throw new ValidationException("Рейтинг MPA с ID " + mpaId + " не существует");
        }
    }

    // Проверка на существование жанра по ID
    private void validateGenreIds(List<Genre> genres) {
        if (genres == null) {
            return;
        }

        for (Genre genre : genres) {
            String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genre.getId());
            if (count == null || count == 0) {
                throw new ValidationException("Жанр с ID " + genre.getId() + " не существует");
            }
        }
    }

    // Маппинг данных в объект Film
    private Film mapRowToFilm(ResultSet rs) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_id");
        MpaRating mpa = new MpaRating();
        mpa.setId(mpaId);
        mpa.setName(getMpaById(mpaId).getName());
        film.setMpa(mpa);

        return film;
    }
}
