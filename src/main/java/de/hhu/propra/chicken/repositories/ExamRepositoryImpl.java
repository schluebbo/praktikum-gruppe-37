package de.hhu.propra.chicken.repositories;

import de.hhu.propra.chicken.domain.model.Exam;
import de.hhu.propra.chicken.domain.service.ExamRepository;
import de.hhu.propra.chicken.repositories.dataRepo.DBExamRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
@SuppressFBWarnings(value = "EI_EXPOSE_REP", justification = "Not important here")
public class ExamRepositoryImpl implements ExamRepository {

    DBExamRepository exams;

    public ExamRepositoryImpl(DBExamRepository exams) {
        this.exams = exams;
    }

    @Override
    public void save(Exam exam) {
        exams.save(exam);
    }

    @Override
    public List<Exam> findAll() {
        return exams.findAll();
    }

    @Override
    public void remove(LocalDate date, LocalTime startTime, LocalTime endTime, String examName, Long lsfID, boolean offline) {
        exams.delete(new Exam(date, startTime, endTime, examName, lsfID, offline));
    }

    //Testing
    @Override
    public void removeAll() {
        exams.deleteAll();
    }

    @Override
    public Optional<Exam> findExam(Long lsfID) {
        return exams.findById(lsfID);
    }

}
