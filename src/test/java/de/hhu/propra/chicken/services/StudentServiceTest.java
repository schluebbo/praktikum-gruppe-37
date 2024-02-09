package de.hhu.propra.chicken.services;

import de.hhu.propra.chicken.domain.model.Exam;
import de.hhu.propra.chicken.domain.model.ExamParticipation;
import de.hhu.propra.chicken.domain.model.Student;
import de.hhu.propra.chicken.domain.model.Vacation;
import de.hhu.propra.chicken.domain.service.StudentRepository;
import de.hhu.propra.chicken.services.messages.Messages;
import de.hhu.propra.chicken.services.values.PropertiesValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.temporal.TemporalAdjusters.next;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StudentServiceTest {

    StudentService studentService;
    @MockBean
    Messages messages;
    @MockBean
    StudentRepository mockedStudentRepository;
    @MockBean
    PropertiesValues mockedPropertiesValues;
    @MockBean
    Messages mockedMessages;


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
        mockedStudentRepository = mock(StudentRepository.class);
        mockedMessages = new Messages(mockedPropertiesValues);
        studentService = new StudentService(mockedPropertiesValues, mockedStudentRepository, mockedMessages);
    }

    private static final String githubName = "John";
    private static final Long githubID = 1015814L;


    LocalTime allowedStartTime = LocalTime.of(9, 30);
    LocalTime allowedEndTime = LocalTime.of(13, 30);
    LocalDate allowedVacationDay = LocalDate.now().with(next(MONDAY));

    @Test
    @DisplayName("Urlaub ist vorhanden")
    void test1() {
        LocalTime allowedStartTime = LocalTime.of(12, 0);
        LocalTime allowedEndTime = LocalTime.of(13, 0);

        //Act
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);

        studentService.addVacation(allowedVacationDay, allowedStartTime, allowedEndTime, githubID, githubName);
        Set<Vacation> output = studentService.getVacationFromStudent(githubID, githubName);
        //Assert
        assertThat(output).isNotEmpty();
    }

    @Test
    @DisplayName("Urlaub ist korrekt gespeichert")
    void test2() {
        //Arrange
        LocalTime allowedStartTime = mockedPropertiesValues.getInternshipStartTime();
        LocalTime allowedEndTime = mockedPropertiesValues.getInternshipEndTime();
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        //Act
        studentService.addVacation(allowedVacationDay, allowedStartTime, allowedEndTime, githubID, githubName);
        Set<Vacation> vacations = studentService.getVacationFromStudent(githubID, githubName);
        //Assert
        assertThat(vacations).contains(new Vacation(allowedVacationDay, allowedStartTime, allowedEndTime, githubID));
    }

    @Test
    @DisplayName("Urlaub beträgt nicht weniger als 15 Minuten")
    void test3() {
        //Arrange

        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);

        LocalTime endzeit = LocalTime.of(9, 35);
        //Act
        HashMap<String,Set<String>> messages = studentService.validateVacation(allowedVacationDay, allowedStartTime, endzeit, githubID, githubName, false);
        //Assert
        assertThat(messages.get("personalErrors")).contains(mockedMessages.vacationTooShort());
    }

    @Test
    @DisplayName("Verfügbare Urlaubszeit wurde überschritten")
    void test4() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        LocalTime endzeit = allowedStartTime.plusMinutes(mockedPropertiesValues.getTotalHolidayTime()).plusMinutes(1);
        //Act
        HashMap<String,Set<String>> messages = studentService.validateVacation(allowedVacationDay, allowedStartTime, endzeit, githubID, githubName, false);
        //Assert
        assertThat(messages.get("personalErrors")).contains(mockedMessages.overTimeLimit());
    }

    @Test
    @DisplayName("Verfügbare Urlaubszeit an unterschiedlichen Tagen wurde überschritten")
    void test5() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        LocalDate zweiterurlaubstag = LocalDate.of(2022, 3, 22);
        LocalTime zweitestartzeit = LocalTime.of(9, 30);
        LocalTime zweiteendzeit = LocalTime.of(13, 0);
        studentService.addVacation(allowedVacationDay, allowedStartTime, allowedEndTime, githubID, githubName);
        //Act
        HashMap<String,Set<String>> messages = studentService.validateVacation(zweiterurlaubstag, zweitestartzeit, zweiteendzeit, githubID, githubName, false);
        //Assert
        assertThat(messages.get("personalErrors")).contains(this.mockedMessages.overTimeLimit());
    }


    @Test
    @DisplayName("Sie können entweder den gesamten Tag frei nehmen, oder bis zu 2.5 Stunden(Bei einem Urlaub)")
    void test6() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);

        LocalTime endzeit = LocalTime.of(12, 30);
        //Act
        HashMap<String,Set<String>> messages = studentService.validateVacation(allowedVacationDay, allowedStartTime, endzeit, githubID, githubName, false);
        //Assert
        assertThat(messages.get("personalErrors")).contains(mockedMessages.tooMuchVacationOnADay());
    }

    @Test
    @DisplayName("Sie können entweder den gesamten Tag frei nehmen, oder bis zu 2.5 Stunden(Bei mehreren Urlauben)")
    void test7() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        LocalTime endzeit = LocalTime.of(11, 30);
        studentService.addVacation(allowedVacationDay, allowedStartTime, endzeit, githubID, githubName);
        LocalTime zweitestartzeit = LocalTime.of(12, 00);
        LocalTime zweiteendzeit = LocalTime.of(13, 00);
        //Act
        HashMap<String,Set<String>> messages = studentService.validateVacation(allowedVacationDay, zweitestartzeit, zweiteendzeit, githubID, githubName, false);
        //Assert
        assertThat(messages.get("personalErrors")).contains(mockedMessages.tooMuchVacationOnADay());
    }

    @Test
    @DisplayName("Sie können maximal zwei Urlaubsblöcke nehmen")
    void test8() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        studentService.addVacation(allowedVacationDay, allowedStartTime, allowedEndTime, githubID, githubName);
        LocalTime endzeit = LocalTime.of(10, 30);
        studentService.addVacation(allowedVacationDay, allowedStartTime, endzeit, githubID, githubName);
        LocalTime zweitestartzeit = LocalTime.of(12, 30);
        LocalTime zweiteendzeit = LocalTime.of(13, 30);
        studentService.addVacation(allowedVacationDay, zweitestartzeit, zweiteendzeit, githubID, githubName);
        LocalTime drittestartzeit = LocalTime.of(11, 0);
        LocalTime dritteendzeit = LocalTime.of(12, 0);
        //Act
        HashMap<String,Set<String>> messages = studentService.validateVacation(allowedVacationDay, drittestartzeit, dritteendzeit, githubID, githubName, false);
        //Assert
        assertThat(messages.get("personalErrors")).contains(mockedMessages.tooManyVacations());
    }

    @Test
    @DisplayName("Falsche Endzeit des späteren Blocks")
    void test9() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        LocalTime endzeit = LocalTime.of(10, 30);
        studentService.addVacation(allowedVacationDay, allowedStartTime, endzeit, githubID, githubName);
        LocalTime zweitestartzeit = LocalTime.of(12, 30);
        LocalTime zweiteendzeit = LocalTime.of(13, 0);
        //Act
        HashMap<String,Set<String>> messages = studentService.validateVacation(allowedVacationDay, zweitestartzeit, zweiteendzeit, githubID, githubName, false);
        //Assert
        assertThat(messages.get("personalErrors")).contains(mockedMessages.wrongStartEndTimes());
    }

    @Test
    @DisplayName("Falsche Endzeit des früheren Blocks")
    void test10() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        LocalTime fruehereStartzeit = LocalTime.of(10, 0);
        LocalTime fruhereEndzeit = LocalTime.of(10, 30);
        LocalTime spaetereStartzeit = LocalTime.of(12, 30);
        LocalTime spaetereEndzeit = LocalTime.of(13, 30);
        studentService.addVacation(allowedVacationDay, spaetereStartzeit, spaetereEndzeit, githubID, githubName);
        //Act
        HashMap<String,Set<String>> messages = studentService.validateVacation(allowedVacationDay, fruehereStartzeit, fruhereEndzeit, githubID, githubName, false);
        //Assert
        assertThat(messages.get("personalErrors")).contains(mockedMessages.wrongStartEndTimes());
    }

    @Test
    @DisplayName("Berechnung der Urlaubsdauer")
    void test11() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        LocalTime startzeit = LocalTime.of(12, 30);
        LocalTime endzeit = LocalTime.of(13, 30);
        LocalDate zweiterurlaubstag = LocalDate.of(2022, 3, 22);
        LocalTime zweiterstartzeit = LocalTime.of(12, 30);
        LocalTime zweiterendzeit = LocalTime.of(13, 30);
        LocalDate dritterurlaubstag = LocalDate.of(2022, 3, 23);
        LocalTime dritterstartzeit = LocalTime.of(12, 30);
        LocalTime dritterendzeit = LocalTime.of(13, 30);
        Long githubID = 1015814L;
        studentService.addVacation(allowedVacationDay, startzeit, endzeit, githubID, githubName);
        studentService.addVacation(zweiterurlaubstag, zweiterstartzeit, zweiterendzeit, githubID, githubName);
        studentService.addVacation(dritterurlaubstag, dritterstartzeit, dritterendzeit, githubID, githubName);
        //Act
        int vacationDuration = studentService.calcVacationDurationFromStudent(githubID, githubName);
        //Assert
        assertThat(vacationDuration).isEqualTo(180L);
    }


    @Test
    @DisplayName("isOverlapping() Methode testen")
    void test12() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        LocalTime startSecond = LocalTime.of(13, 0);
        LocalTime endSecond = LocalTime.of(14, 0);

        LocalTime startThird = LocalTime.of(14, 0);
        LocalTime endThird = LocalTime.of(16, 59);

        Long githubID = 1015814L;
        studentService.addVacation(allowedVacationDay, startTime, endTime, githubID, githubName);
        studentService.addVacation(allowedVacationDay, startSecond, endSecond, githubID, githubName);
        //Act
        HashMap<String,Set<String>> messages = studentService.validateVacation(allowedVacationDay, startThird, endThird, githubID, githubName, false);
        //Assert
        assertThat(messages.get("personalErrors")).contains(mockedMessages.overlapping());
    }


    @Test
    @DisplayName("remove() Methode testen")
    void test13() {
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        studentService.addVacation(allowedVacationDay, allowedStartTime, allowedEndTime, githubID, githubName);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        LocalTime startTimeToRemove = LocalTime.of(13, 0);
        LocalTime endTimeToRemove = LocalTime.of(13, 30);

        LocalDate now = LocalDate.now();
        LocalDate dateToRemove = now.plusDays(2);

        Long githubID = 1015814L;
        studentService.addVacation(now, startTime, endTime, githubID, githubName);
        studentService.addVacation(dateToRemove, startTimeToRemove, endTimeToRemove, githubID, githubName);
        //Act
        boolean exists = studentService.removeVacationFromStudent(dateToRemove, startTimeToRemove, endTimeToRemove, githubID, githubName);
        //Assert
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("remove() Methode testen schlägt fehl (Kein Urlaub vorhanden mit dem Datum)")
    void test14() {
        //Arrange
        Student studentWithVacations = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(studentWithVacations);

        Student studentWithNoVacations = new Student(10158141425L, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(studentWithNoVacations.getGithubID(), "Johnny")).thenReturn(studentWithNoVacations);

        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        LocalTime startSecond = LocalTime.of(13, 0);
        LocalTime endSecond = LocalTime.of(13, 30);

        LocalDate now = LocalDate.now();
        LocalDate dateToRemove = now.plusDays(2);

        studentService.addVacation(now, startTime, endTime, studentWithVacations.getGithubID(), githubName);
        studentService.addVacation(dateToRemove, startSecond, endSecond, studentWithVacations.getGithubID(), githubName);
        //Act
        boolean exists = studentService.removeVacationFromStudent(dateToRemove, startSecond, endSecond, studentWithNoVacations.getGithubID(), "Johnny");
        //Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("remove() Methode testen schlägt fehl (Urlaub liegt vor heutigem Tag)")
    void test15() {
        //Arrange
        Student studentWithVacations = new Student(10158141425L, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(studentWithVacations.getGithubID(), "Johnny")).thenReturn(studentWithVacations);

        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(11, 0);

        LocalTime startSecond = LocalTime.of(13, 0);
        LocalTime endSecond = LocalTime.of(13, 30);

        LocalDate now = LocalDate.now();
        LocalDate dateToRemove = now.minusDays(2);

        studentService.addVacation(now, startTime, endTime, studentWithVacations.getGithubID(), "Johnny");
        studentService.addVacation(dateToRemove, startSecond, endSecond, studentWithVacations.getGithubID(), "Johnny");
        //Act
        boolean exists = studentService.removeVacationFromStudent(dateToRemove, startSecond, endSecond, studentWithVacations.getGithubID(), "Johnny");
        //Assert
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Urlaube überschneiden sich")
    void test16() {
        //Arrange
        Student johnny = new Student(10158141425L, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(johnny);
        LocalDate date = LocalDate.of(2022, 3, 17);
        LocalTime startFirstTime = LocalTime.of(10, 0);
        LocalTime endFirstTime = LocalTime.of(12, 0);
        Vacation firstVacation = new Vacation(date, startFirstTime, endFirstTime, johnny.getGithubID());
        LocalTime startSecondTime = LocalTime.of(14, 0);
        LocalTime endSecondTime = LocalTime.of(16, 0);
        Vacation secondVacation = new Vacation(date, startSecondTime, endSecondTime, johnny.getGithubID());
        Set<Vacation> johnnysVacations = Set.of(firstVacation, secondVacation);

        LocalTime startTimeNewVacation = LocalTime.of(9, 0);
        LocalTime endTimeNewVacation = LocalTime.of(17, 0);

        //Act
        boolean overlapping = studentService.vacationIsOverlapping(startTimeNewVacation, endTimeNewVacation, johnnysVacations);
        //Assert
        assertThat(overlapping).isTrue();
    }

    @Test
    @DisplayName("Klausur wird in die Mitte eines Urlaubs eingetragen und es existieren zwei Urlaube")
    void test17a() {
        //Arrange
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);
        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime startVacationTime = LocalTime.of(9, 30);
        LocalTime endVacationTime = LocalTime.of(13, 30);
        studentService.addVacation(date, startVacationTime, endVacationTime, githubID, "Johnny");

        LocalTime startExamTime = LocalTime.of(10, 30);
        LocalTime endExamTime = LocalTime.of(12, 30);
        long lsfID = 1234L;
        String examName = "examName";
        Exam exam = new Exam(date, startExamTime, endExamTime, examName, lsfID, false);

        //Act
        studentService.addExamParticipation(githubID, exam, "Johnny");
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny").size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Klausur wird in die Mitte eines Urlaubs eingetragen und erster Urlaub ist korrekt")
    void test17b() {
        //Arrange
        LocalTime internshipStartTime = mockedPropertiesValues.getInternshipStartTime();
        int bonusTimeOnline = mockedPropertiesValues.getBonusTimeOnline();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();

        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);
        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime startVacationTime = LocalTime.of(9, 30);
        LocalTime endVacationTime = LocalTime.of(13, 30);
        studentService.addVacation(date, startVacationTime, endVacationTime, githubID, "Johnny");

        LocalTime startExamTime = LocalTime.of(10, 30);
        LocalTime endExamTime = LocalTime.of(12, 30);
        long lsfID = 1234L;
        String examName = "examName";
        Exam exam = new Exam(date, startExamTime, endExamTime, examName, lsfID, false);
        Vacation vacation = new Vacation(date, startVacationTime, exam.getStartExemption(internshipStartTime, bonusTimeOffline, bonusTimeOnline), githubID);

        //Act
        studentService.addExamParticipation(githubID, exam, "Johnny");
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny")).contains(vacation);
    }

    @Test
    @DisplayName("Klausur wird in die Mitte eines Urlaubs eingetragen und zweiter Urlaub ist korrekt")
    void test17c() {
        //Arrange
        LocalTime internshipEndTime = mockedPropertiesValues.getInternshipEndTime();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);
        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime startVacationTime = LocalTime.of(9, 30);
        LocalTime endVacationTime = LocalTime.of(13, 30);
        studentService.addVacation(date, startVacationTime, endVacationTime, githubID, "Johnny");

        LocalTime startExamTime = LocalTime.of(10, 30);
        LocalTime endExamTime = LocalTime.of(12, 30);
        long lsfID = 1234L;
        String examName = "examName";
        Exam exam = new Exam(date, startExamTime, endExamTime, examName, lsfID, false);

        //Act
        studentService.addExamParticipation(githubID, exam, "Johnny");
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny")).contains(new Vacation(date, exam.getEndExemption(internshipEndTime, bonusTimeOffline), endVacationTime, githubID));
    }

    @Test
    @DisplayName("Ende vom Urlaub ist waehrend der neu eingetragenen Klausur")
    void test18() {
        //Arrange
        LocalTime internshipStartTime = mockedPropertiesValues.getInternshipStartTime();
        int bonusTimeOnline = mockedPropertiesValues.getBonusTimeOnline();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 30);
        LocalTime to = LocalTime.of(13, 30);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);

        LocalTime startFirstTime = LocalTime.of(9, 0);
        LocalTime endFirstTime = LocalTime.of(12, 0);
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        //Act
        studentService.addExamParticipation(githubID, exam, "Johnny");
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny").iterator().next().getEndTime()).isEqualTo(exam.getStartExemption(internshipStartTime, bonusTimeOffline, bonusTimeOnline));
    }

    @Test
    @DisplayName("Start vom Urlaub ist während einer neu eingetragenen Klausur")
    void test19() {
        //Arrange
        LocalTime internshipEndTime = mockedPropertiesValues.getInternshipEndTime();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(10, 30);
        LocalTime to = LocalTime.of(11, 30);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);

        LocalTime startFirstTime = LocalTime.of(10, 30);
        LocalTime endFirstTime = LocalTime.of(12, 30);
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        //Act
        studentService.addExamParticipation(githubID, exam, "Johnny");
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny").iterator().next().getStartTime()).isEqualTo(exam.getEndExemption(internshipEndTime, bonusTimeOffline));
    }

    @Test
    @DisplayName("Start und Ende vom Urlaub ist während einer neu eingetragenen Klausur")
    void test20() {
        //Arrange
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(10, 00);
        LocalTime to = LocalTime.of(12, 00);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);

        LocalTime startFirstTime = LocalTime.of(10, 30);
        LocalTime endFirstTime = LocalTime.of(11, 00);
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        //Act
        studentService.addExamParticipation(githubID, exam, "Johnny");
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny")).isEmpty();
    }

    @Test
    @DisplayName("Start und Ende von einer Klausur umschließen neu eingetragenen Ulraub")
    void test21() {
        //Arrange
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(10, 00);
        LocalTime to = LocalTime.of(12, 00);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        List<Exam> examListByDate = new ArrayList<>();
        examListByDate.add(exam);

        LocalTime startFirstTime = LocalTime.of(10, 30);
        LocalTime endFirstTime = LocalTime.of(11, 00);
        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");
        studentService.refreshExams(date, startFirstTime, endFirstTime, githubID, "Johnny", examListByDate);
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny")).isEmpty();
    }


    @Test
    @DisplayName("Start und Ende von einer Klausur liegen während einer neu eingetragenen Ulraub")
    void test22a() {
        //Arrange
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 00);
        LocalTime to = LocalTime.of(12, 00);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        List<Exam> examListByDate = new ArrayList<>();
        examListByDate.add(exam);

        LocalTime startFirstTime = LocalTime.of(9, 30);
        LocalTime endFirstTime = LocalTime.of(13, 00);
        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");
        studentService.refreshExams(date, startFirstTime, endFirstTime, githubID, "Johnny", examListByDate);
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny").size()).isEqualTo(2);
    }

    @Test
    @DisplayName("Start und Ende von einer Klausur liegen während einer neu eingetragenen Ulraub")
    void test22b() {
        //Arrange
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);
        LocalTime internshipEndTime = mockedPropertiesValues.getInternshipEndTime();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 00);
        LocalTime to = LocalTime.of(12, 00);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        List<Exam> examListByDate = new ArrayList<>();
        examListByDate.add(exam);

        LocalTime startFirstTime = LocalTime.of(9, 30);
        LocalTime endFirstTime = LocalTime.of(13, 00);
        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");
        studentService.refreshExams(date, startFirstTime, endFirstTime, githubID, "Johnny", examListByDate);
        //Assert

        assertThat(studentService.getVacationFromStudent(githubID, "Johnny")).contains(new Vacation(date, exam.getEndExemption(internshipEndTime, bonusTimeOffline), endFirstTime, githubID));
    }

    @Test
    @DisplayName("Start und Ende von einer Klausur liegen während einer neu eingetragenen Ulraub")
    void test22c() {
        //Arrange
        LocalTime internshipStartTime = mockedPropertiesValues.getInternshipStartTime();
        int bonusTimeOnline = mockedPropertiesValues.getBonusTimeOnline();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 00);
        LocalTime to = LocalTime.of(12, 00);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        List<Exam> examListByDate = new ArrayList<>();
        examListByDate.add(exam);

        LocalTime startFirstTime = LocalTime.of(9, 30);
        LocalTime endFirstTime = LocalTime.of(13, 00);
        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");
        studentService.refreshExams(date, startFirstTime, endFirstTime, githubID, "Johnny", examListByDate);
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny")).contains(new Vacation(date, startFirstTime, exam.getStartExemption(internshipStartTime, bonusTimeOffline, bonusTimeOnline), githubID));
    }

    @Test
    @DisplayName("Start einer Klausur ist während einer neu eingetragenen Ulraub")
    void test23() {
        //Arrange
        LocalTime internshipStartTime = mockedPropertiesValues.getInternshipStartTime();
        int bonusTimeOnline = mockedPropertiesValues.getBonusTimeOnline();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 00);
        LocalTime to = LocalTime.of(12, 00);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        List<Exam> examListByDate = new ArrayList<>();
        examListByDate.add(exam);

        LocalTime startFirstTime = LocalTime.of(9, 30);
        LocalTime endFirstTime = LocalTime.of(11, 00);
        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");
        studentService.refreshExams(date, startFirstTime, endFirstTime, githubID, "Johnny", examListByDate);
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny").iterator().next().getEndTime()).isEqualTo(exam.getStartExemption(internshipStartTime, bonusTimeOffline, bonusTimeOnline));
    }

    @Test
    @DisplayName("Ende einer Klausur ist während einer neu eingetragenen Urlaub")
    void test24() {
        //Arrange
        LocalTime internshipEndTime = mockedPropertiesValues.getInternshipEndTime();
        int bonusTimeOffline = mockedPropertiesValues.getBonusTimeOffline();
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 00);
        LocalTime to = LocalTime.of(13, 00);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        List<Exam> examListByDate = new ArrayList<>();
        examListByDate.add(exam);

        LocalTime startFirstTime = LocalTime.of(12, 30);
        LocalTime endFirstTime = LocalTime.of(13, 30);
        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");
        studentService.refreshExams(date, startFirstTime, endFirstTime, githubID, "Johnny", examListByDate);
        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny").iterator().next().getStartTime()).isEqualTo(exam.getEndExemption(internshipEndTime, bonusTimeOffline));
    }

    @Test
    @DisplayName("Urlaub darf an Klausurtagen frei gewählt werden")
    void test25() {
        //Arrange
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 00);
        LocalTime to = LocalTime.of(11, 30);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);

        //Assert
        LocalTime startFirstTime = LocalTime.of(9, 30);
        LocalTime endFirstTime = LocalTime.of(10, 30);
        LocalTime startSecondTime = LocalTime.of(11, 30);
        LocalTime endSecondTime = LocalTime.of(13, 15);
        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");

        HashMap<String,Set<String>> messages = studentService.validateVacation(date, startSecondTime, endSecondTime, githubID, "Johnny", true);

        //Assert
        assertThat(messages).isEmpty();
    }

    @Test
    @DisplayName("Urlaub darf an Klausurtagen frei gewählt werden aber Overlapping darf trotzdem nicht funktionieren!")
    void test26() {
        //Arrange
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 00);
        LocalTime to = LocalTime.of(11, 30);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);


        LocalTime startFirstTime = LocalTime.of(9, 30);
        LocalTime endFirstTime = LocalTime.of(10, 30);
        LocalTime startSecondTime = LocalTime.of(10, 00);
        LocalTime endSecondTime = LocalTime.of(13, 15);
        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");
        HashMap<String,Set<String>> messages = studentService.validateVacation(date, startSecondTime, endSecondTime, githubID, "Johnny", true);
        //Assert
        assertThat(messages.get("personalErrors")).contains(mockedMessages.overlapping());
    }

    @Test
    @DisplayName("Stornieren der letzten Klausur an einem Tag, löscht alle Urlaube dieses Tages")
    void test27() {
        //Arrange
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 00);
        LocalTime to = LocalTime.of(11, 30);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        Set<Exam> examListByDate = new HashSet<>();
        examListByDate.add(exam);

        LocalTime startFirstTime = LocalTime.of(9, 30);
        LocalTime endFirstTime = LocalTime.of(10, 30);

        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");
        studentService.removeExamParticipationFromStudent(date, lsfID, githubID, "Johnny");
        studentService.removeVacationsIfNoMoreParticipations(githubID, "Johnny", examListByDate, date);

        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny")).isEmpty();
    }

    @Test
    @DisplayName("Urlaube sollen nicht storniert wrden, wenn noch eine Klausur am selben Tag vorhanden ist")
    void test28() {
        //Arrange
        Student johnny = new Student(githubID, "Johnny", new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, "Johnny")).thenReturn(johnny);

        LocalDate date = LocalDate.now().with(next(MONDAY));
        LocalTime from = LocalTime.of(11, 00);
        LocalTime to = LocalTime.of(11, 30);
        String examName = "Lehren der Mathematischen Wunder";
        long lsfID = 1234L;
        boolean offline = false;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        Set<Exam> examListByDate = new HashSet<>();
        examListByDate.add(exam);


        LocalTime secondFrom = LocalTime.of(12, 00);
        LocalTime secondTo = LocalTime.of(12, 30);
        String secondExamName = "Lehren der Biologischen Wunder";
        long secondLsfID = 228599L;
        boolean secondOffline = false;

        Exam secondExam = new Exam(date, secondFrom, secondTo, secondExamName, secondLsfID, secondOffline);
        examListByDate.add(secondExam);
        LocalTime startFirstTime = LocalTime.of(9, 30);
        LocalTime endFirstTime = LocalTime.of(10, 30);

        //Act
        studentService.addVacation(date, startFirstTime, endFirstTime, githubID, "Johnny");
        studentService.addExamParticipation(githubID, exam, "Johnny");
        studentService.addExamParticipation(githubID, secondExam, "Johnny");
        studentService.removeExamParticipationFromStudent(date, lsfID, githubID, "Johnny");
        studentService.removeVacationsIfNoMoreParticipations(githubID, "Johnny", examListByDate, date);

        //Assert
        assertThat(studentService.getVacationFromStudent(githubID, "Johnny")).isNotEmpty();
    }

    @Test
    @DisplayName("Urlaube dürfen nicht am Wochenende genommen werden")
    void test29() {
        //Act
        LocalDate allowedVacationDay = LocalDate.now().with(next(SUNDAY));
        Set<String> messages = studentService.validateVacationDate(allowedVacationDay);
        //Assert
        assertThat(messages).contains(mockedMessages.weekend());
    }

    @Test
    @DisplayName("Urlaube dürfen nicht vor dem Praktikumsbeginn genommen werden")
    void test30() {
        LocalDate tooEarlyVacationDay = LocalDate.of(1000, 1, 1);
        //Act
        Set<String> messages = studentService.validateVacationDate(tooEarlyVacationDay);
        //Assert
        assertThat(messages).contains(mockedMessages.dateToEarly());
    }

    @Test
    @DisplayName("Urlaube dürfen nicht nach Praktikumsende genommen werden")
    void test31() {
        LocalDate tooLateVacationDay = mockedPropertiesValues.getInternshipEndDate().plusDays(1);
        //Act
        Set<String> messages = studentService.validateVacationDate(tooLateVacationDay);
        //Assert
        assertThat(messages).contains(mockedMessages.dateTooLate());
    }

    @Test
    @DisplayName("Urlaube dürfen nicht vor Startzeit des Praktikums genommen werden")
    void test32() {
        //Act
        LocalTime illegalStartTime = mockedPropertiesValues.getInternshipStartTime().minusHours(1);
        Set<String> messages = studentService.validateVacationStartTime(illegalStartTime);
        //Assert
        assertThat(messages).contains(mockedMessages.timeToEarly());
    }

    @Test
    @DisplayName("Urlaube dürfen nicht nach 13:30h genommen werden")
    void test33() {
        //Act
        LocalTime illegalEndTime = mockedPropertiesValues.getInternshipEndTime().plusHours(1);
        Set<String> messages = studentService.validateVacationEndTime(illegalEndTime);
        //Assert
        assertThat(messages).contains(mockedMessages.timeTooLate());
    }

    @Test
    @DisplayName("Urlaubsbeginn muss Vielfaches startTime 15 Minuten sein")
    void test34() {
        LocalTime illegalStartTime = LocalTime.of(9, 33);
        //Act
        Set<String> messages = studentService.validateVacationStartTime(illegalStartTime);
        //Assert
        assertThat(messages).contains(mockedMessages.wrongBlockStart());
    }

    @Test
    @DisplayName("Urlaubsende muss Vielfaches startTime 15 Minuten sein")
    void test35() {
        LocalTime illegalEndTime = LocalTime.of(13, 23);
        //Act
        Set<String> messages = studentService.validateVacationEndTime(illegalEndTime);
        //Assert
        assertThat(messages).contains(mockedMessages.wrongBlockEnd());
    }

    @Test
    @DisplayName("Urlaub darf nicht vor heutigem Tag liegen")
    void test36() {
        //Arrange
        LocalDate yesterday = LocalDate.now().minusDays(1);
        //Act
        Set<String> messages = studentService.validateVacationDate(yesterday);
        //Assert
        assertThat(messages).contains(mockedMessages.dateIsBeforeToday());
    }

    @Test
    @DisplayName("studentRegisteredToExam() Methode überprüft korrekt, ob ein Student bereits angemeldet ist")
    void test37(){
        //Arrange
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        when(mockedStudentRepository.findStudent(githubID, githubName)).thenReturn(student);
        
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;
        Exam exam = new Exam(date, from, to, examName, lsf_id, offline);

        studentService.addExamParticipation(githubID, exam, githubName);
        //Act
        HashMap<String, Set<String>> hashMap = studentService.studentRegisteredToExam(githubID, githubName, exam);
        //Assert
        assertThat(hashMap).containsKey("registerError");
    }

}
