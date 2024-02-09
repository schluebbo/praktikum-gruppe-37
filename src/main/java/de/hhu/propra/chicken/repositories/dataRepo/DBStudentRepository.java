package de.hhu.propra.chicken.repositories.dataRepo;

import de.hhu.propra.chicken.domain.model.Student;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DBStudentRepository extends CrudRepository<Student, Long> {
    List<Student> findAll();

    Student findStudentByGithubID(Long githubID);
}
