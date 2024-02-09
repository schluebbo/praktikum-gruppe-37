package de.hhu.propra.chicken.repositories.dataRepo;

import de.hhu.propra.chicken.domain.model.Exam;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface DBExamRepository extends CrudRepository<Exam, Long> {
    List<Exam> findAll();
}
