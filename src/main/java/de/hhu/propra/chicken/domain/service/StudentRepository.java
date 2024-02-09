package de.hhu.propra.chicken.domain.service;

import de.hhu.propra.chicken.domain.model.Student;

import java.util.List;

public interface StudentRepository {
    void save(Student student);

    Student findStudent(Long githubID, String githubName);

    Student findStudentByGithubID(Long githubID);

    List<Student> getStudents();

    //Testing
    void removeAll();


}
