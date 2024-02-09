package de.hhu.propra.chicken.web.forms;

import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public record CancelVacationForm(@NotNull @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                                 @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime startTime,
                                 @NotNull @DateTimeFormat(pattern = "HH:mm") LocalTime endTime) {

}
