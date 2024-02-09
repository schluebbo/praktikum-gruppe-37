package de.hhu.propra.chicken.repositories;

import de.hhu.propra.chicken.domain.model.Student;
import de.hhu.propra.chicken.domain.service.StudentRepository;
import de.hhu.propra.chicken.repositories.dataRepo.DBStudentRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class StudentRepositoryImpl implements StudentRepository {

    DBStudentRepository students;

    public StudentRepositoryImpl(DBStudentRepository students) {
        this.students = students;
    }

    @Override
    public void save(Student student) {
        students.save(student);
    }

    @Override
    public void removeAll() {
        students.deleteAll();
    }

    @Override
    public Student findStudent(Long githubID, String githubName) {
        Student student = findStudentByGithubID(githubID);
        //Optional<Student> student = students.findById(githubID);
        return student;
    }

    @Override
    public Student findStudentByGithubID(Long githubID) {
        return students.findStudentByGithubID(githubID);
    }

    public List<Student> getStudents() {
        return students.findAll();
    }
}
