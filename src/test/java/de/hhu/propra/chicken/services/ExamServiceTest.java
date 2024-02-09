package de.hhu.propra.chicken.services;

import de.hhu.propra.chicken.domain.model.Exam;
import de.hhu.propra.chicken.domain.model.ExamParticipation;
import de.hhu.propra.chicken.domain.service.ExamRepository;
import de.hhu.propra.chicken.dto.ExamInfo;
import de.hhu.propra.chicken.services.messages.Messages;
import de.hhu.propra.chicken.services.values.PropertiesValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExamServiceTest {

    ExamService examService;
    @MockBean
    ExamRepository examRepository;
    @MockBean
    PropertiesValues propertiesValues;
    @MockBean
    Messages messages;


    @BeforeEach
    void mockAll() {
        propertiesValues = mock(PropertiesValues.class);
        when(propertiesValues.getMinMinutes()).thenReturn(15);
        when(propertiesValues.getInternshipStartDate()).thenReturn(LocalDate.now().minusDays(10));
        when(propertiesValues.getInternshipEndDate()).thenReturn(LocalDate.now().plusDays(10));
        when(propertiesValues.getInternshipStartTime()).thenReturn(LocalTime.of(9, 30));
        when(propertiesValues.getInternshipEndTime()).thenReturn(LocalTime.of(13, 30));
        when(propertiesValues.getTotalHolidayTime()).thenReturn(240);
        when(propertiesValues.getMaxHolidayBlockLength()).thenReturn(150);
        when(propertiesValues.getMaxNumberVacationOneDay()).thenReturn(2);
        when(propertiesValues.getExamStartTime()).thenReturn(LocalTime.of(0, 0, 1));
        when(propertiesValues.getExamEndTime()).thenReturn(LocalTime.of(23, 59, 59));
        when(propertiesValues.getBonusTimeOnline()).thenReturn(30);
        when(propertiesValues.getBonusTimeOffline()).thenReturn(120);
        examRepository = mock(ExamRepository.class);
        messages = new Messages(propertiesValues);
        examService = new ExamService(propertiesValues, examRepository, messages);
    }


    @Test
    @DisplayName("Klausur ist korrekt gespeichert")
    void test3() {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;
        Exam exam = new Exam(date, from, to, examName, lsf_id, offline);
        when(examRepository.findAll()).thenReturn(new ArrayList<>(List.of(exam)));
        //Act
        examService.addExam(exam);
        List<ExamInfo> allExamsInfo = examService.getAllExamsInfo();
        //Assert
        assertThat(allExamsInfo.get(0).lsfId()).isEqualTo(lsf_id);

    }

    @Test
    @DisplayName("ExamInfo ist vorhanden nach dem hinzufuegen einer Klausur")
    void test5() throws NoSuchFieldException, IllegalAccessException {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsfID = 1234L;
        boolean offline = true;
        LocalTime internshipStartTime = propertiesValues.getInternshipStartTime();
        LocalTime internshipEndTime = propertiesValues.getInternshipEndTime();
        int bonusTimeOnline = propertiesValues.getBonusTimeOnline();
        int bonusTimeOffline = propertiesValues.getBonusTimeOffline();
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        when(examRepository.findAll()).thenReturn(List.of(exam));

        LocalTime endExemption = exam.getEndExemption(internshipEndTime, bonusTimeOffline);
        LocalTime startExemption = exam.getStartExemption(internshipStartTime, bonusTimeOffline, bonusTimeOnline);

        ExamInfo examInfo = new ExamInfo(lsfID, examName, date, from, to, offline, startExemption, endExemption, exam.isCancelable());

        examService.addExam(exam);

        //Act
        List<ExamInfo> allExamsInfo = examService.getAllExamsInfo();
        //Assert
        assertThat(allExamsInfo.get(0)).isEqualTo(examInfo);
    }

    @Test
    @DisplayName("ExamInfo bekommen zu einer lsf_id")
    void test6() throws NoSuchFieldException, IllegalAccessException {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsfID = 1234L;
        Long lsfID2 = 12345L;
        boolean offline = true;
        boolean cancelable = true;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        Exam exam2 = new Exam(date, from, to, examName, lsfID2, offline);
        LocalTime startExemption = exam.getStartExemption(
                propertiesValues.getInternshipStartTime(),
                propertiesValues.getBonusTimeOffline(),
                propertiesValues.getBonusTimeOnline());
        LocalTime endExemption = exam.getEndExemption(propertiesValues.getInternshipEndTime(), propertiesValues.getBonusTimeOffline());

        ExamInfo examInfo1 = new ExamInfo(lsfID, examName, date, from, to, offline, startExemption, endExemption, cancelable);
        examService.addExam(exam);
        when(examRepository.findAll()).thenReturn(List.of(exam, exam2));
        //Act
        ExamInfo examInfo3 = examService.getExamsInfoLsfID(lsfID);
        //Assert
        assertThat(examInfo3).isEqualTo(examInfo1);
    }

    @Test
    @DisplayName("ifLsfIDExistsInRepo Methode funktioniert korrekt")
    void test7() {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;
        when(examRepository.findAll()).thenReturn(List.of(new Exam(date, from, to, examName, lsf_id, offline)));
        //Act

        boolean lsfIDExistsInRepo = examService.ifLsfIDExistsInRepo(lsf_id);
        //Assert
        assertThat(lsfIDExistsInRepo).isTrue();

    }

    @Test
    @DisplayName("examByLsfID Methode funktioniert korrekt")
    void test8() {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;
        when(examRepository.findAll()).thenReturn(List.of(new Exam(date, from, to, examName, lsf_id, offline)));
        //Act

        Exam exam = examService.examByLsfID(lsf_id);
        //Assert
        assertThat(exam).isEqualTo(new Exam(date, from, to, examName, lsf_id, offline));

    }

    @Test
    @DisplayName("getExamsFrom Methode funktioniert")
    void test9() {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsfID = 1234L;
        boolean offline = true;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        ExamParticipation examParticipation = new ExamParticipation(lsfID);
        Set<ExamParticipation> examParticipations = Set.of(examParticipation);
        when(examRepository.findAll()).thenReturn(List.of(exam));

        //Act
        List<Exam> exams = examService.getExamsFrom(examParticipations);
        //Assert
        assertThat(exams.get(0)).isEqualTo(exam);
    }

    @Test
    @DisplayName("getExamDateByLsfID Methode funktioniert korrekt")
    void test10() {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;
        when(examRepository.findAll()).thenReturn(List.of(new Exam(date, from, to, examName, lsf_id, offline)));
        //Act

        LocalDate expectedDate = examService.getExamDateByLsfID(lsf_id);

        //Assert
        assertThat(expectedDate).isEqualTo(date);

    }

    @Test
    @DisplayName("getExamByLsfID Methode funktioniert korrekt")
    void test11() {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;
        when(examRepository.findAll()).thenReturn(List.of(new Exam(date, from, to, examName, lsf_id, offline)));
        //Act

        Exam examByLsfID = examService.getExamByLsfID(lsf_id);

        //Assert
        assertThat(examByLsfID).isEqualTo(new Exam(date, from, to, examName, lsf_id, offline));

    }

    @Test
    @DisplayName("getExamsByParticipationsAndDate Methode funktioniert")
    void test12() {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsfID = 1234L;
        boolean offline = true;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);

        ExamParticipation examParticipation = new ExamParticipation(lsfID);
        Set<ExamParticipation> examParticipations = Set.of(examParticipation);
        when(examRepository.findAll()).thenReturn(List.of(exam));

        //Act
        List<Exam> exams = examService.getExamsByParticipationsAndDate(examParticipations, date);
        //Assert
        assertThat(exams.get(0)).isEqualTo(exam);
    }

    @Test
    @DisplayName("getExamsByDate Methode funktioniert korrekt")
    void test13() {
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;
        when(examRepository.findAll()).thenReturn(List.of(new Exam(date, from, to, examName, lsf_id, offline)));
        //Act

        Set<Exam> examsByDate = examService.getExamsByDate(date);

        //Assert
        assertThat(examsByDate.iterator().next()).isEqualTo(new Exam(date, from, to, examName, lsf_id, offline));

    }

    @Test
    @DisplayName("ValidateExam() Methode funktioniert korrekt anhand valider Exam")
    void test14(){
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;

        //Act
        HashMap<String, Set<String>> hashMap = examService.validateExam(lsf_id, date, from, to);

        //Assert
        assertThat(hashMap.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("ValidateExam() Methode funktioniert korrekt anhand invalider Exam (falsche Startzeit)")
    void test15(){
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 12);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;

        //Act
        HashMap<String, Set<String>> hashMap = examService.validateExam(lsf_id, date, from, to);

        //Assert
        assertThat(hashMap).containsKey("startErrors");
    }

    @Test
    @DisplayName("getExamInfoFrom() Methode funktioniert korrekt")
    void test16(){
        //Arrange
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsf_id = 1234L;
        boolean offline = true;

        ExamParticipation examParticipation = new ExamParticipation(lsf_id);
        Set<ExamParticipation> examParticipations = new HashSet<>();
        examParticipations.add(examParticipation);

        when(examRepository.findAll()).thenReturn(List.of(new Exam(date, from, to, examName, lsf_id, offline)));

        //Act
        List<ExamInfo> examInfos = examService.getExamInfoFrom(examParticipations);

        //Assert
        assertThat(examInfos.get(0).lsfId()).isEqualTo(lsf_id);
    }


}
