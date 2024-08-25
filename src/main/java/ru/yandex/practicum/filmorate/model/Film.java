package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;

@Data
public class Film {

    @PositiveOrZero
    private int id;

    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    @PastOrPresent
    private LocalDate releaseDate;

    @Positive
    private int duration;
}
