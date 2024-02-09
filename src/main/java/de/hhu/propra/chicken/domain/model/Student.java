package de.hhu.propra.chicken.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Table("STUDENT")
public class Student {
    @Version
    @Column("STUDENT_VERSION")
    Long student_version;
    @Id
    @Column("GITHUB_ID")
    private final Long githubID;
    @Column("GITHUB_NAME")
    private final String github_name;
    private final Set<ExamParticipation> exam_participation;
    private final Set<Vacation> vacations;

    public Student(Long githubID, String github_name, Set<ExamParticipation> exam_participation, Set<Vacation> vacations) {
        this.github_name = github_name;
        this.githubID = githubID;
        this.exam_participation = new HashSet<ExamParticipation>(exam_participation);
        this.vacations = new HashSet<Vacation>(vacations);
    }

    public void saveVacation(Vacation vacation) {
        vacations.add(vacation);
    }

    public void removeVacation(LocalDate date, LocalTime from, LocalTime to) {
        vacations.remove(new Vacation(date, from, to, this.githubID));
    }

    public Set<ExamParticipation> getExam_participation() {
        return Set.copyOf(exam_participation);
    }

    public Set<Vacation> getVacations() {
        return Set.copyOf(vacations);
    }

    public String getGithub_name() {
        return github_name;
    }

    public Long getGithubID() {
        return githubID;
    }


    public void saveExamParticipation(Long lsf_id) {
        exam_participation.add(new ExamParticipation(lsf_id));
    }

    public void removeExamParticipation(Long lsfID) {
        ExamParticipation examParticipation = exam_participation.stream().filter(e -> e.getLsfID().equals(lsfID)).findFirst().get();
        exam_participation.remove(examParticipation);
    }
}
