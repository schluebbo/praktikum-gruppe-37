package de.hhu.propra.chicken.domain.model;

import de.hhu.propra.chicken.services.values.PropertiesValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExamTest {

    PropertiesValues mockedPropertiesValues;

    @BeforeEach
    void mockAll() {
        mockedPropertiesValues = mock(PropertiesValues.class);
        when(mockedPropertiesValues.getMinMinutes()).thenReturn(15);
        when(mockedPropertiesValues.getInternshipStartDate()).thenReturn(LocalDate.now().minusDays(10));
        when(mockedPropertiesValues.getInternshipEndDate()).thenReturn(LocalDate.now().plusDays(10));
        when(mockedPropertiesValues.getInternshipStartTime()).thenReturn(LocalTime.of(9, 30));
        when(mockedPropertiesValues.getInternshipEndTime()).thenReturn(LocalTime.of(13, 30));
        when(mockedPropertiesValues.getTotalHolidayTime()).thenReturn(240);
        when(mockedPropertiesValues.getMaxHolidayBlockLength()).thenReturn(150);
        when(mockedPropertiesValues.getMaxNumberVacationOneDay()).thenReturn(2);
        when(mockedPropertiesValues.getExamStartTime()).thenReturn(LocalTime.of(0, 0, 1));
        when(mockedPropertiesValues.getExamEndTime()).thenReturn(LocalTime.of(23, 59, 59));
        when(mockedPropertiesValues.getBonusTimeOnline()).thenReturn(30);
        when(mockedPropertiesValues.getBonusTimeOffline()).thenReturn(120);
    }

    @Test
    @DisplayName("Richtige Freistellungsstartzeit offline wird zurueckgegeben (vor Praktikumsstartzeit)")
    void test1() {
        //Arrange
        LocalTime internshipStartTime = mockedPropertiesValues.getInternshipStartTime();
        int bonusTimeOnline = mockedPropertiesValues.getBonusTimeOnline();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();

        Long lsfID = 1234L;
        LocalDate date = LocalDate.now();
        LocalTime examStartTime = LocalTime.of(9, 0);
        LocalTime examEndTime = LocalTime.of(12, 0);
        String examName = "ProPra";
        boolean offline = true;
        Exam exam = new Exam(date, examStartTime, examEndTime, examName, lsfID, offline);

        //Act
        LocalTime startExemption = exam.getStartExemption(internshipStartTime, bonusTimeOffline, bonusTimeOnline);

        //Assert
        assertThat(startExemption).isEqualTo(internshipStartTime);
    }

    @Test
    @DisplayName("Richtige Freistellungsstartzeit offline wird zurueckgegeben (nach Praktikumsstartzeit)")
    void test2() {
        //Arrange
        LocalTime internshipStartTime = mockedPropertiesValues.getInternshipStartTime();
        int bonusTimeOnline = mockedPropertiesValues.getBonusTimeOnline();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Long lsfID = 1234L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(11, 45);
        LocalTime endTime = LocalTime.of(12, 0);
        String examName = "ProPra";
        boolean offline = true;
        Exam exam = new Exam(date, startTime, endTime, examName, lsfID, offline);

        //Act
        LocalTime startExemption = exam.getStartExemption(internshipStartTime, bonusTimeOffline, bonusTimeOnline);

        //Assert
        assertThat(startExemption).isEqualTo(startTime.minusMinutes(bonusTimeOffline));
    }

    @Test
    @DisplayName("Richtige Freistellungsstartzeit online wird zurueckgegeben (vor Praktikumsstartzeit)")
    void test3() {
        //Arrange
        LocalTime internshipStartTime = mockedPropertiesValues.getInternshipStartTime();
        int bonusTimeOnline = mockedPropertiesValues.getBonusTimeOnline();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Long lsfID = 1234L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(12, 0);
        String examName = "ProPra";
        boolean offline = false;
        Exam exam = new Exam(date, startTime, endTime, examName, lsfID, offline);

        //Act
        LocalTime startExemption = exam.getStartExemption(internshipStartTime, bonusTimeOffline, bonusTimeOnline);

        //Assert
        assertThat(startExemption).isEqualTo(internshipStartTime);
    }

    @Test
    @DisplayName("Richtige Freistellungsstartzeit online wird zurueckgegeben (nach Praktikumsstartzeit)")
    void test4() {
        //Arrange
        LocalTime internshipStartTime = mockedPropertiesValues.getInternshipStartTime();
        int bonusTimeOnline = mockedPropertiesValues.getBonusTimeOnline();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Long lsfID = 1234L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(11, 45);
        LocalTime endTime = LocalTime.of(12, 0);
        String examName = "ProPra";
        boolean offline = false;
        Exam exam = new Exam(date, startTime, endTime, examName, lsfID, offline);

        //Act
        LocalTime startExemption = exam.getStartExemption(internshipStartTime, bonusTimeOffline, bonusTimeOnline);

        //Assert
        assertThat(startExemption).isEqualTo(startTime.minusMinutes(bonusTimeOnline));
    }

    @Test
    @DisplayName("Richtige Freistellungsendezeit offline wird zurueckgegeben (nach Praktikumsendzeit)")
    void test5() {
        //Arrange
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        LocalTime internshipEndTime = mockedPropertiesValues.getInternshipEndTime();
        Long lsfID = 1234L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(16, 0);
        String examName = "ProPra";
        boolean offline = true;
        Exam exam = new Exam(date, startTime, endTime, examName, lsfID, offline);

        //Act
        LocalTime endExemption = exam.getEndExemption(internshipEndTime, bonusTimeOffline);

        //Assert
        assertThat(endExemption).isEqualTo(internshipEndTime);
    }

    @Test
    @DisplayName("Richtige Freistellungsendezeit offline wird zurueckgegeben (vor Praktikumsendzeit)")
    void test6() {
        //Arrange
        LocalTime internshipEndTime = mockedPropertiesValues.getInternshipEndTime();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Long lsfID = 1234L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        String examName = "ProPra";
        boolean offline = true;
        Exam exam = new Exam(date, startTime, endTime, examName, lsfID, offline);

        //Act
        LocalTime endExemption = exam.getEndExemption(internshipEndTime, bonusTimeOffline);

        //Assert
        assertThat(endExemption).isEqualTo(endTime.plusMinutes(bonusTimeOffline));
    }

    @Test
    @DisplayName("Richtige Freistellungsendezeit online wird zurueckgegeben (nach Praktikumsendzeit)")
    void test7() {
        //Arrange
        LocalTime internshipEndTime = mockedPropertiesValues.getInternshipEndTime();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Long lsfID = 1234L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(16, 0);
        String examName = "ProPra";
        boolean offline = false;
        Exam exam = new Exam(date, startTime, endTime, examName, lsfID, offline);

        //Act
        LocalTime endExemption = exam.getEndExemption(internshipEndTime, bonusTimeOffline);

        //Assert
        assertThat(endExemption).isEqualTo(internshipEndTime);
    }

    @Test
    @DisplayName("Richtige Freistellungsendezeit online wird zurueckgegeben (vor Praktikumsendzeit)")
    void test8() {
        //Arrange
        LocalTime internshipEndTime = mockedPropertiesValues.getInternshipEndTime();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Long lsfID = 1234L;
        LocalDate date = LocalDate.now();
        LocalTime startTime = LocalTime.of(9, 0);
        LocalTime endTime = LocalTime.of(11, 0);
        String examName = "ProPra";
        boolean offline = false;
        Exam exam = new Exam(date, startTime, endTime, examName, lsfID, offline);

        //Act
        LocalTime endExemption = exam.getEndExemption(internshipEndTime, bonusTimeOffline);

        //Assert
        assertThat(endExemption).isEqualTo(endTime);
    }
}
