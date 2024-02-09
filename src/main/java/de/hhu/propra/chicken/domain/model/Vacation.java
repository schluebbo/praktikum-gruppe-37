package de.hhu.propra.chicken.domain.model;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import static java.time.temporal.ChronoUnit.MINUTES;

@Table("VACATION")
public final class Vacation {

    @Column("VACATION_DATE")
    private final LocalDate vacation_date;
    @Column("START_TIME")
    private final LocalTime startTime;
    @Column("END_TIME")
    private final LocalTime endTime;
    @Column("GITHUB_ID")
    private final Long github_id;

    public Vacation(LocalDate vacation_date, LocalTime startTime, LocalTime endTime, Long github_id) {
        this.vacation_date = vacation_date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.github_id = github_id;
    }

    public long getDuration() {
        return MINUTES.between(startTime, endTime);
    }

    public Long getGithub_id() {
        return github_id;
    }


    public LocalDate getVacation_date() {
        return vacation_date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vacation)) return false;
        Vacation vacation = (Vacation) o;
        return getVacation_date().equals(vacation.getVacation_date()) && getStartTime().equals(vacation.getStartTime()) && getEndTime().equals(vacation.getEndTime()) && getGithub_id().equals(vacation.getGithub_id());
    }

    public boolean isCancelable() {
        return vacation_date.isAfter(LocalDate.now());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getVacation_date(), getStartTime(), getEndTime(), getGithub_id());
    }
}
