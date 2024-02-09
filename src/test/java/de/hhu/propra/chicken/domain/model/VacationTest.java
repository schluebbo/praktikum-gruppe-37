package de.hhu.propra.chicken.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class VacationTest {

    @Test
    @DisplayName("Dauer eines Urlaubs richtig berechnet")
    void test1() {
        //Arrange
        LocalDate date = LocalDate.of(2022, 3, 11);
        LocalTime from = LocalTime.of(12, 0);
        LocalTime to = LocalTime.of(13, 0);
        Vacation vacation = new Vacation(date, from, to, null);
        //Act
        long duration = vacation.getDuration();
        //Assert
        assertThat(duration).isEqualTo(60);
    }

    @Test
    @DisplayName("Urlaube sind gleich")
    void test2() {
        //Arrange
        LocalDate date = LocalDate.of(2022, 3, 11);
        LocalTime from = LocalTime.of(12, 0);
        LocalTime to = LocalTime.of(13, 0);
        Vacation vacation = new Vacation(date, from, to, 1015814L);
        LocalDate secondDate = LocalDate.of(2022, 3, 11);
        LocalTime secondFrom = LocalTime.of(12, 0);
        LocalTime secondTo = LocalTime.of(13, 0);
        Vacation secondVacation = new Vacation(secondDate, secondFrom, secondTo, 1015814L);
        //Act
        boolean isEquals = vacation.equals(secondVacation);
        //Assert
        assertThat(isEquals).isTrue();
    }

    @Test
    @DisplayName("Urlaube in der vergangenheit sind nicht loeschbar")
    void test3() {
        //Arrange
        LocalDate vacationDate = LocalDate.now().minusDays(1);
        LocalTime startTime = LocalTime.of(12, 0);
        LocalTime endTime = LocalTime.of(13, 0);
        Vacation vacation = new Vacation(vacationDate, startTime, endTime, 1015814L);
        //Act
        boolean isCancelable = vacation.isCancelable();
        //Assert
        assertThat(isCancelable).isFalse();
    }
}
