package de.hhu.propra.chicken.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamParticipationInfo(Long lsfID, String examName, LocalDate date, LocalTime startTime, LocalTime endTime,
                                    boolean offline, Long numberOfParticipants) {
}
