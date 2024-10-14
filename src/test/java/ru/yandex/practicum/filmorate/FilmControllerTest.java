package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    private Film film;

    @BeforeEach
    public void setUp() {
        film = new Film();
        film.setName("Название фильма");
        film.setDescription("Описание фильма");
        film.setReleaseDate(LocalDate.of(2020, 2, 20));
        film.setDuration(180);
    }

    @Test
    public void testBlankName() throws Exception {
        film.setName("");

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testTooLongDescription() throws Exception {
        film.setDescription("A".repeat(201));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testTooEarlyReleaseDate() throws Exception {
        film.setReleaseDate(LocalDate.of(1800, 1, 1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testNegativeDuration() throws Exception {
        film.setDuration(-180);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testValidFilm() throws Exception {
        Film savedFilm = new Film();
        savedFilm.setId(1);
        savedFilm.setName(film.getName());
        savedFilm.setDescription(film.getDescription());
        savedFilm.setReleaseDate(film.getReleaseDate());
        savedFilm.setDuration(film.getDuration());

        when(filmService.addFilm(any(Film.class))).thenReturn(savedFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedFilm.getId()))
                .andExpect(jsonPath("$.name").value(savedFilm.getName()))
                .andExpect(jsonPath("$.description").value(savedFilm.getDescription()))
                .andExpect(jsonPath("$.releaseDate").value(savedFilm.getReleaseDate().toString()))
                .andExpect(jsonPath("$.duration").value(savedFilm.getDuration()));
    }

    @Test
    public void testServiceThrowsValidationException() throws Exception {
        film.setName("");

        when(filmService.addFilm(any(Film.class)))
                .thenThrow(new ValidationException("Название фильма не может быть пустым"));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }
}
