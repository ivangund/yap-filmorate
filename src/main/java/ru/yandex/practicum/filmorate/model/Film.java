package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.ValidReleaseDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {

    @PositiveOrZero
    private int id;

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 200)
    private String description;

    @ValidReleaseDate
    private LocalDate releaseDate;

    @Positive
    private int duration;

    private List<Genre> genres;

    private MpaRating mpa;
}
