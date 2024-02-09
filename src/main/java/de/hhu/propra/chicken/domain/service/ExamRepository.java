package de.hhu.propra.chicken.domain.service;

import de.hhu.propra.chicken.domain.model.Exam;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ExamRepository {
    void save(Exam exam);

    List<Exam> findAll();

    void remove(LocalDate date, LocalTime startTime, LocalTime endTime, String examName, Long lsfID, boolean offline);

    //Testing
    void removeAll();

    Optional<Exam> findExam(Long lsfID);
}
