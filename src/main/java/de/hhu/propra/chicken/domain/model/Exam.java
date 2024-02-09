package de.hhu.propra.chicken.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Table("EXAM")
public class Exam {
    @Version
    @Column("EXAM_VERSION")
    Integer exam_version;
    @Id
    @Column("LSF_ID")
    private final Long lsf_id;
    @Column("EXAM_DATE")
    private final LocalDate exam_date;
    @Column("EXAM_START")
    private final LocalTime exam_start;
    @Column("EXAM_END")
    private final LocalTime exam_end;
    @Column("EXAM_NAME")
    private final String exam_name;
    @Column("EXAM_OFFLINE")
    private final boolean offline;

    public Exam(LocalDate exam_date, LocalTime exam_start, LocalTime exam_end, String exam_name, Long lsf_id, boolean offline) {
        this.exam_date = exam_date;
        this.exam_start = exam_start;
        this.exam_end = exam_end;
        this.exam_name = exam_name;
        this.lsf_id = lsf_id;
        this.offline = offline;
    }


    public LocalTime getStartExemption(LocalTime start_time, int bonusTimeOffline, int bonusTimeOnline) {
        if (offline) {
            LocalTime offlineStartTime = exam_start.minusMinutes(bonusTimeOffline);
            if (offlineStartTime.isBefore(start_time)) {
                return start_time;
            } else {
                return exam_start.minusMinutes(bonusTimeOffline);
            }
        } else {
            LocalTime onlineStartTime = exam_start.minusMinutes(bonusTimeOnline);
            if (onlineStartTime.isBefore(start_time)) {
                return start_time;
            } else {
                return onlineStartTime;
            }
        }
    }

    public LocalTime getEndExemption(LocalTime endTime, int bonusTimeOffline) {
        if (offline) {
            LocalTime offlineEndTime = exam_end.plusMinutes(bonusTimeOffline);
            if (offlineEndTime.isAfter(endTime)) {
                return endTime;
            } else {
                return offlineEndTime;
            }
        } else {
            if (exam_end.isAfter(endTime)) {
                return endTime;
            }
            return exam_end;
        }
    }


    public LocalDate getExam_date() {
        return exam_date;
    }

    public LocalTime getExam_start() {
        return exam_start;
    }

    public LocalTime getExam_end() {
        return exam_end;
    }

    public String getExam_name() {
        return exam_name;
    }

    public Long getLsf_id() {
        return lsf_id;
    }

    public boolean isOffline() {
        return offline;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Exam exam1)) return false;
        return getLsf_id().equals(exam1.getLsf_id()) && isOffline() == exam1.isOffline() && getExam_date().equals(exam1.getExam_date()) && getExam_start().equals(exam1.getExam_start()) && getExam_end().equals(exam1.getExam_end()) && getExam_name().equals(exam1.getExam_name());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getExam_date(), getExam_start(), getExam_end(), getExam_name(), getLsf_id(), isOffline());
    }

    public boolean isCancelable() {
        return exam_date.isAfter(LocalDate.now());
    }
}