package de.hhu.propra.chicken.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamInfo(Long lsfId, String examName, LocalDate examDate, LocalTime examStartTime, LocalTime examEndTime,
                       boolean offline, LocalTime startExemption, LocalTime endExemption, boolean cancelable) {
}

