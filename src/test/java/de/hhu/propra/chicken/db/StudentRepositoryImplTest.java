package de.hhu.propra.chicken.db;

import de.hhu.propra.chicken.domain.model.ExamParticipation;
import de.hhu.propra.chicken.domain.model.Student;
import de.hhu.propra.chicken.domain.model.Vacation;
import de.hhu.propra.chicken.domain.service.StudentRepository;
import de.hhu.propra.chicken.repositories.StudentRepositoryImpl;
import de.hhu.propra.chicken.repositories.dataRepo.DBStudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@ActiveProfiles("test")
public class StudentRepositoryImplTest {

    @Autowired
    private DBStudentRepository stDB;

    StudentRepository studentRepository;

    @BeforeEach
    void setup() {
        studentRepository = new StudentRepositoryImpl(stDB);
    }

    @Test
    @DisplayName("Student wird korrekt gespeichert")
    @Sql("classpath:V1__initTest.sql")
    void test1() {
        Long githubID = 283300L;
        String githubName = "Rüdiger";
        Student student = new Student(githubID, githubName, new HashSet<ExamParticipation>(), new HashSet<Vacation>());
        studentRepository.save(student);

        Student studentFound = stDB.findById(283300L).orElse(null);

        assertThat(studentFound.getGithubID()).isEqualTo(githubID);
    }

    @Test
    @DisplayName("Student wird korrekt aus der Datenbank gelesen")
    @Sql({"classpath:V1__initTest.sql", "classpath:V2__dataTest.sql"})
    void test2() {
        //Arrange
        Long githubID = 123456L;
        String githubName = "Johnny";
        //Act
        Student studentFound = stDB.findById(123456L).orElse(null);
        //Assert
        assertThat(studentFound.getGithubID()).isEqualTo(githubID);
    }

    @Test
    @DisplayName("Alle Student-Objekte werden korrekt aus der Datenbank gelesen")
    @Sql({"classpath:V1__initTest.sql", "classpath:V2__dataTest.sql"})
    void test3() {
        List<Student> studentFound = stDB.findAll();

        assertThat(studentFound.size()).isEqualTo(3);
    }

    //TODO: Repo removeByID hinzufügen
    /*@Test
    @DisplayName("Student wird korrekt gelöscht")
    @Sql({"classpath:V1__initTest.sql", "classpath:V2__dataTest.sql"})
    void test4(){
        Long githubID = 123456L;
        String githubName = "Johnny";
        studentRepository.remove();
        //Act
        Student studentFound = stDB.findById(123456L).orElse(null);
        //Assert
        assertThat(studentFound).isEqualTo(null);
    }*/
}

