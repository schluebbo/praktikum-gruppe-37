package de.hhu.propra.chicken.services.values;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class PropertiesValues {
    @Value("${values.min_minutes}")
    int minMinutes;
    @Value("${values.start_date}")
    public String internshipStartDate;
    @Value("${values.end_date}")
    String internshipEndDate;
    @Value("${values.start_time}")
    public String internshipStartTime;
    @Value("${values.end_time}")
    String internshipEndTime;
    @Value("${values.total_holiday_time}")
    int totalHolidayTime;
    @Value("${values.max_holiday_block_length}")
    int maxHolidayBlockLength;
    @Value("${values.max_number_vacation_one_day}")
    int maxNumberVacationOneDay;
    @Value("${values.exam_start_time}")
    String examStartTime;
    @Value("${values.exam_end_time}")
    String examEndTime;
    @Value("${values.bonus_time_offline}")
    int bonusTimeOffline;
    @Value("${values.bonus_time_online}")
    int bonusTimeOnline;

    public int getMinMinutes() {
        return minMinutes;
    }

    public LocalDate getInternshipStartDate() {
        return LocalDate.parse(internshipStartDate);
    }

    public LocalDate getInternshipEndDate() {
        return LocalDate.parse(internshipEndDate);
    }

    public LocalTime getInternshipStartTime() {
        return LocalTime.parse(internshipStartTime);
    }

    public LocalTime getInternshipEndTime() {
        return LocalTime.parse(internshipEndTime);
    }

    public int getTotalHolidayTime() {
        return totalHolidayTime;
    }

    public int getMaxHolidayBlockLength() {
        return maxHolidayBlockLength;
    }

    public int getMaxNumberVacationOneDay() {
        return maxNumberVacationOneDay;
    }

    public LocalTime getExamStartTime() {
        return LocalTime.parse(examStartTime);
    }

    public LocalTime getExamEndTime() {
        return LocalTime.parse(examEndTime);
    }

    public int getBonusTimeOffline() {
        return bonusTimeOffline;
    }

    public int getBonusTimeOnline() {
        return bonusTimeOnline;
    }
}
