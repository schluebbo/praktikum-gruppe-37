package de.hhu.propra.chicken.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class StudentTest {

    Long githubID = 1015814L;
    String githubName = "John";

    @Test
    @DisplayName("Urlaub ist nach dem Speichern vorhanden")
    void test1() {

        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime startTime = LocalTime.of(9, 30);
        LocalTime endTime = LocalTime.of(12, 0);
        Vacation vacation = new Vacation(date, startTime, endTime, githubID);

        //Act
        student.saveVacation(vacation);

        //Assert
        assertThat(student.getVacations()).isNotEmpty();
    }

    @Test
    @DisplayName("remove() Methode testen")
    void test2() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime startTime = LocalTime.of(9, 30);
        LocalTime endTime = LocalTime.of(12, 0);
        Vacation vacation = new Vacation(date, startTime, endTime, githubID);
        student.saveVacation(vacation);
        //Act
        student.removeVacation(date, startTime, endTime);

        //Assert
        assertThat(student.getVacations()).isEmpty();
    }
}
