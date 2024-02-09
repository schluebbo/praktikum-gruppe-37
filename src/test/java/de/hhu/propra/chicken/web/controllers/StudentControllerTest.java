package de.hhu.propra.chicken.web.controllers;

import de.hhu.propra.chicken.domain.model.Exam;
import de.hhu.propra.chicken.domain.model.ExamParticipation;
import de.hhu.propra.chicken.domain.model.Student;
import de.hhu.propra.chicken.domain.model.Vacation;
import de.hhu.propra.chicken.services.ExamService;
import de.hhu.propra.chicken.services.LogService;
import de.hhu.propra.chicken.services.StudentService;
import de.hhu.propra.chicken.services.values.PropertiesValues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.time.DayOfWeek.MONDAY;
import static java.time.temporal.TemporalAdjusters.next;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StudentControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    PropertiesValues propertiesValues;

    @MockBean
    ExamService examService;
    @MockBean
    StudentService studentService;
    @MockBean
    LogService logService;

    @BeforeEach
    void setup() {
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
    }


    @Test
    @DisplayName("Nach Post auf vacationRegistration wird redirected auf index")
    void test6() throws Exception {
        LocalDate date = LocalDate.of(1980, 1, 1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedDateString = date.format(dateTimeFormatter);

        HashMap<String, Set<String>> entrySet = new HashMap<>();
        Set<ExamParticipation> examParticipations = new HashSet<>();
        List<Exam> exams = new ArrayList<>();


        when(studentService.validateVacation(any(),any(),any(),any(),any(), anyBoolean())).thenReturn(entrySet);
        when(studentService.getExamParticipationsFromStudent(any(), any())).thenReturn(examParticipations);
        when(examService.getExamsByParticipationsAndDate(any(), any())).thenReturn(exams);

        //Act, Assert
        mvc.perform(post("/vacationRegistration")
                        .param("date", formattedDateString)
                        .param("startTime", "09:30")
                        .param("endTime", "10:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student"));
    }

    @Test
    @DisplayName("Nach Post auf vacationRegistration mit falschen Daten wird redirected auf index")
    void test7() throws Exception {
        LocalDate date = LocalDate.of(1980, 1, 1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDateString = date.format(dateTimeFormatter);

        HashMap<String, Set<String>> entrySet = new HashMap<>();
        entrySet.put("error", Collections.emptySet());
        when(studentService.validateVacation(any(), any(), any(), any(), any(), anyBoolean())).thenReturn(entrySet);

        //Act, Assert
        mvc.perform(post("/vacationRegistration")
                        .param("date", formattedDateString)
                        .param("startTime", "09:30")
                        .param("endTime", "10:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vacationRegistration"));
    }

    @Test
    @DisplayName("Nach Post auf vacationRegistration mit korrekten Daten wird redirected auf student")
    void test8() throws Exception {
        LocalDate date = LocalDate.of(1980, 1, 1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedDateString = date.format(dateTimeFormatter);

        //Act, Assert
        mvc.perform(post("/vacationRegistration")
                        .param("date", formattedDateString)
                        .param("startTime", "09:30")
                        .param("endTime", "10:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student"));
    }

    @Test
    @DisplayName("Nach Post auf cancelVacation wird redirected auf student")
    void test9() throws Exception {
        LocalDate date = LocalDate.of(1980, 1, 1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDateString = date.format(dateTimeFormatter);

        //Act, Assert
        mvc.perform(post("/cancelVacation")
                        .param("date", formattedDateString)
                        .param("startTime", "09:30")
                        .param("endTime", "10:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student"));
    }

    @Test
    @DisplayName("Nach Post auf createExam mit falschen Daten wird redirected auf createExam")
    void test10() throws Exception {
        LocalDate date = LocalDate.of(1980, 1, 1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDateString = date.format(dateTimeFormatter);

        //Act, Assert
        mvc.perform(post("/createExam")
                        .param("date", formattedDateString)
                        .param("startTime", "09:30")
                        .param("endTime", "10:30"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/createExam"));
    }

    @Test
    @DisplayName("Nach Post auf createExam mit korrekten Daten wird redirected auf student")
    void test11() throws Exception {
        LocalDate date = LocalDate.of(1980, 1, 1);
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedDateString = date.format(dateTimeFormatter);
        HashMap<String, Set<String>> entrySet = new HashMap<>();
        when(examService.validateExam(any(), any(), any(), any())).thenReturn(entrySet);

        //Act, Assert
        mvc.perform(post("/createExam")
                        .param("date", formattedDateString)
                        .param("startTime", "09:30")
                        .param("endTime", "10:30")
                        .param("examName", "ProPra")
                        .param("lsfID", "1234")
                        .param("offline", "true")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/examRegistration"));
    }


    @Test
    @DisplayName("Nach Post auf examRegistration mit falschen Daten wird redirected auf examRegistration")
    void test12() throws Exception {
        HashMap<String, Set<String>> entrySet = new HashMap<>();

        Exam exam = new Exam(LocalDate.now(),LocalTime.now(),LocalTime.now(),"ProPra",1234L,false);

        when(examService.validateExam(any(), any(), any(), any())).thenReturn(entrySet);
        when(examService.getExamByLsfID(any())).thenReturn(exam);
        when(studentService.studentRegisteredToExam(any(), any(), any())).thenReturn(entrySet);

        //Act, Assert
        mvc.perform(post("/examRegistration")
                        .param("lsfID", "-1234")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/examRegistration"));
    }

    @Test
    @DisplayName("Nach Post auf examRegistration mit falschen Daten wird redirected auf student")
    void test13() throws Exception {
        HashMap<String, Set<String>> entrySet = new HashMap<>();

        Exam exam = new Exam(LocalDate.now(),LocalTime.now(),LocalTime.now(),"ProPra",1234L,false);

        when(examService.validateExam(any(), any(), any(), any())).thenReturn(entrySet);
        when(examService.getExamByLsfID(any())).thenReturn(exam);
        when(studentService.studentRegisteredToExam(any(), any(), any())).thenReturn(entrySet);

        //Act, Assert
        mvc.perform(post("/examRegistration")
                        .param("lsfID", "1234")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student"));
    }

    @Test
    @DisplayName("Nach Post auf examParticipation/{lsfID}/delete wird redirected auf student")
    void test14() throws Exception {
        LocalDate date = LocalDate.of(1980, 1, 1);
        Set<Exam> examList = Collections.emptySet();

        when(examService.getExamDateByLsfID(any())).thenReturn(date);
        when(examService.getExamsByDate(any())).thenReturn(examList);

        //Act, Assert
        mvc.perform(post("/examParticipation/1234/delete")
                        .param("lsfID", "1234")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student"));
    }

    @Test
    @DisplayName("Add Urlaub wird aufgerufen (Nach POST)")
    void test15() throws Exception {
        //Arrange
        LocalDate date = LocalDate.now().with(next(MONDAY));
        HashMap<String, Set<String>> entrySet = new HashMap<>();
        when(studentService.validateVacation(any(), any(), any(), any(), any(), anyBoolean())).thenReturn(entrySet);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        String formattedDateString = date.format(dateTimeFormatter);

        //Act
        mvc.perform(post("/vacationRegistration")
                .param("date", formattedDateString)
                .param("startTime", "09:30")
                .param("endTime", "10:30"));
        //Assert
        verify(studentService, times(1)).addVacation(any(), any(), any(), any(), any());

    }

    @Test
    @DisplayName("Wenn storniert wird, wird im Service remove() aufgerufen")
    void test16() throws Exception {
        //Arrange
        Long githubID = 1015814L;
        String githubName = "John";
        LocalDate date = LocalDate.of(1980, 1, 1);
        LocalTime startTime = LocalTime.of(10, 0);
        LocalTime endTime = LocalTime.of(12, 0);

        Vacation vacation = new Vacation(date, startTime, endTime, githubID);
        when(studentService.getVacationFromStudent(githubID, githubName)).thenReturn(Set.of(vacation));

        //Act
        mvc.perform(post("/cancelVacation")
                .flashAttr("githubName", githubName)
                .flashAttr("githubID", githubID.toString())
                .param("date", date.toString())
                .param("startTime", startTime.toString())
                .param("endTime", endTime.toString()));

        //Assert
        verify(studentService, times(1)).removeVacationFromStudent(date, startTime, endTime, githubID, githubName);
    }
}
