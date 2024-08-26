package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;

@Data
public class User {

    @PositiveOrZero
    private int id;

    @NotBlank
    @Email
    @Size(max = 200)
    private String email;

    @NotBlank
    @Pattern(regexp = "\\S+")
    @Size(max = 20)
    private String login;

    @Size(max = 200)
    private String name;

    @Past
    private LocalDate birthday;
}
