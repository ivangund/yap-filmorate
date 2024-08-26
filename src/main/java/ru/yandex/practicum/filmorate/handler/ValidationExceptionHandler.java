package ru.yandex.practicum.filmorate.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Поле '%s': %s", error.getField(),
                        error.getDefaultMessage()))
                .collect(Collectors.joining("; ", "Ошибки валидации: [", "]"));
        log.warn(errorMessage);

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
