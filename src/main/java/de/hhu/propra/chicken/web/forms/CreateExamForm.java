package de.hhu.propra.chicken.web.forms;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.LocalTime;

public record CreateExamForm(@NotNull @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date,
                             @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                             @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime endTime,
                             @NotBlank String examName,
                             @NotNull @Positive Long lsfID,
                             @NotNull boolean offline) {
}