package de.hhu.propra.chicken.db;

import de.hhu.propra.chicken.domain.model.Exam;
import de.hhu.propra.chicken.domain.service.ExamRepository;
import de.hhu.propra.chicken.repositories.ExamRepositoryImpl;
import de.hhu.propra.chicken.repositories.dataRepo.DBExamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@ActiveProfiles("test")
public class ExamRepositoryImplTest {

    @Autowired
    private DBExamRepository exDB;

    ExamRepository examRepository;

    @BeforeEach
    void setup() {
        examRepository = new ExamRepositoryImpl(exDB);
    }

    @Test
    @DisplayName("Exam wird korrekt gespeichert")
    @Sql("classpath:V1__initTest.sql")
    void test1() {
        LocalDate date = LocalDate.now().plusDays(2);
        LocalTime from = LocalTime.of(9, 0);
        LocalTime to = LocalTime.of(12, 0);
        String examName = "ProPra";
        Long lsfID = 7632L;
        boolean offline = true;
        Exam exam = new Exam(date, from, to, examName, lsfID, offline);
        examRepository.save(exam);

        Exam examFound = exDB.findById(7632L).orElse(null);

        assertThat(examFound).isEqualTo(exam);
    }

    @Test
    @DisplayName("Exam wird korrekt aus der Datenbank gelesen")
    @Sql({"classpath:V1__initTest.sql", "classpath:V2__dataTest.sql"})
    void test2() {
        //Arrange
        LocalDate date = LocalDate.of(2022, 03, 22);
        LocalTime from = LocalTime.of(11, 30);
        LocalTime to = LocalTime.of(12, 30);
        String examName = "Lehren der Physik";
        Long lsfID = 5422L;
        boolean offline = true;
        //Act
        Exam examFound = exDB.findById(5422L).orElse(null);
        //Assert
        assertThat(examFound).isEqualTo(new Exam(date, from, to, examName, lsfID, offline));
    }

    @Test
    @DisplayName("Alle Exam-Objekte werden korrekt aus der Datenbank gelesen")
    @Sql({"classpath:V1__initTest.sql", "classpath:V2__dataTest.sql"})
    void test3() {
        List<Exam> examFound = exDB.findAll();

        assertThat(examFound.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("Exam wird korrekt gelöscht")
    @Sql({"classpath:V1__initTest.sql", "classpath:V2__dataTest.sql"})
    void test4() {
        //Arrange
        LocalDate date = LocalDate.of(2022, 03, 22);
        LocalTime from = LocalTime.of(11, 30);
        LocalTime to = LocalTime.of(12, 30);
        String examName = "Lehren der Physik";
        Long lsfID = 5422L;
        boolean offline = true;
        examRepository.remove(date, from, to, examName, lsfID, offline);
        //Act
        Exam examFound = exDB.findById(5422L).orElse(null);
        //Assert
        assertThat(examFound).isEqualTo(null);
    }
}

