package de.hhu.propra.chicken.services;

import de.hhu.propra.chicken.domain.model.Exam;
import de.hhu.propra.chicken.domain.model.ExamParticipation;
import de.hhu.propra.chicken.domain.model.Student;
import de.hhu.propra.chicken.domain.model.Vacation;
import de.hhu.propra.chicken.domain.service.StudentRepository;
import de.hhu.propra.chicken.services.messages.Messages;
import de.hhu.propra.chicken.services.values.PropertiesValues;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class StudentService {

    private final PropertiesValues VALUES;
    private final StudentRepository studentRepository;
    private final Messages messages;

    public StudentService(PropertiesValues propertiesValues, StudentRepository studentRepository, Messages messages) {
        this.VALUES = propertiesValues;
        this.studentRepository = studentRepository;
        this.messages = messages;
    }

    public void addVacation(LocalDate date, LocalTime from, LocalTime to, Long githubID, String githubName) {
        if (studentRepository.findStudent(githubID, githubName) == null) {
            studentRepository.save(new Student(githubID, githubName, new HashSet<>(), new HashSet<>()));
        }

        Student student = studentRepository.findStudent(githubID, githubName);
        student.saveVacation(new Vacation(date, from, to, githubID));
        studentRepository.save(student);
    }

    public Set<Vacation> getVacationFromStudent(Long githubID, String githubName) {
        Student student = studentRepository.findStudent(githubID, githubName);
        return student.getVacations();
    }

    public int calcVacationDurationFromStudent(Long githubID, String githubName) {
        Student student = studentRepository.findStudent(githubID, githubName);
        Set<Vacation> vacations = student.getVacations();
        long sum = vacations.stream().mapToLong(Vacation::getDuration).sum();
        return (int) sum;
    }

    public int calcRemainingVacationFromStudent(Long githubID, String githubName) {
        Student student = studentRepository.findStudent(githubID, githubName);
        Set<Vacation> vacations = student.getVacations();
        long sum = vacations.stream().mapToLong(Vacation::getDuration).sum();
        return VALUES.getTotalHolidayTime() - (int) sum;
    }

    private Set<Vacation> getVacationsOnSameDay(LocalDate date, Long githubID, String githubName) {
        Student student = studentRepository.findStudent(githubID, githubName);
        return student.getVacations().stream()
                .filter(vacation -> vacation.getVacation_date().equals(date))
                .collect(Collectors.toSet());
    }

    private boolean urlaubExists(LocalDate date, LocalTime from, LocalTime to, Long githubID, String githubName) {
        Student student = studentRepository.findStudent(githubID, githubName);
        return student.getVacations().stream()
                .anyMatch(vacation -> vacation.equals(new Vacation(date, from, to, githubID)));
    }

    public boolean removeVacationFromStudent(LocalDate date, LocalTime from, LocalTime to, Long githubID, String githubName) {
        Student student = studentRepository.findStudent(githubID, githubName);
        boolean urlaubExists = urlaubExists(date, from, to, githubID, githubName);
        boolean dateAfterToday = date.isAfter(LocalDate.now());
        boolean urlaubExistiertUndTagLiegtNachHeute = urlaubExists && dateAfterToday;
        if (urlaubExistiertUndTagLiegtNachHeute) {
            student.removeVacation(date, from, to);
            studentRepository.save(student);
            return true;
        }
        return false;
    }

    public HashMap<String, Set<String>> validateVacationAtExamDay(LocalDate date, LocalTime startTime, LocalTime endTime, Long githubID, String githubName) {
        Set<Vacation> vacationsOnSameDay = getVacationsOnSameDay(date, githubID, githubName);
        Set<String> messageSet = new HashSet<>();
        HashMap<String, Set<String>> messages = new HashMap<>();
        int remainingVacation = calcRemainingVacationFromStudent(githubID, githubName);
        if (vacationIsOverlapping(startTime, endTime, vacationsOnSameDay)) {
            messageSet.add(this.messages.overlapping());
        }
        if (timeQuotaUsedUp(startTime, endTime, remainingVacation)) {
            messageSet.add(this.messages.overTimeLimit());
        }

        if(messageSet.isEmpty()){
            return new HashMap<>();
        }
        else{
            messages.put("dateErrors", Collections.emptySet());
            messages.put("startErrors", Collections.emptySet());
            messages.put("endErrors", Collections.emptySet());
            messages.put("personalErrors", messageSet);
            return messages;
        }
    }

    public HashMap<String, Set<String>> validateVacation(LocalDate date, LocalTime startTime, LocalTime endTime, Long githubID, String githubName, boolean hasExamOnDay) {
        if(hasExamOnDay){
            return validateVacationAtExamDay(date, startTime, endTime, githubID, githubName);
        }
        HashMap<String, Set<String>> messages = new HashMap<>();
        Set<String> dateMessages = validateVacationDate(date);
        Set<String> startTimeMessages = validateVacationStartTime(startTime);
        Set<String> endTimeMessages = validateVacationEndTime(endTime);
        Set<String> personalMessages = validatePersonalDetails(date, startTime, endTime, githubID, githubName);
        if (dateMessages.isEmpty() && startTimeMessages.isEmpty() && endTimeMessages.isEmpty() && personalMessages.isEmpty() ) {
            return new HashMap<>();
        } else {
            messages.put("dateErrors", dateMessages);
            messages.put("startErrors", startTimeMessages);
            messages.put("endErrors", endTimeMessages);
            messages.put("personalErrors", personalMessages);
            return messages;
        }
    }

    public Set<String> validateVacationEndTime(LocalTime endTime) {
        Set<String> vacationMessagesSet = new HashSet<>();
        if (!timeIsInRange(endTime)) {
            vacationMessagesSet.add(messages.timeTooLate());
        }

        if (!wholeQuarterHour(endTime.getMinute())) {
            vacationMessagesSet.add(messages.wrongBlockEnd());
        }
        return vacationMessagesSet;
    }

    public Set<String> validateVacationStartTime(LocalTime startTime) {
        Set<String> vacationMessagesSet = new HashSet<>();
        if (!timeIsInRange(startTime)) {
            vacationMessagesSet.add(messages.timeToEarly());
        }
        if (!wholeQuarterHour(startTime.getMinute())) {
            vacationMessagesSet.add(messages.wrongBlockStart());
        }
        return vacationMessagesSet;
    }

    private boolean wholeQuarterHour(int minutes) {
        return minutes % VALUES.getMinMinutes() == 0;
    }

    private boolean timeIsInRange(LocalTime time) {
        return time.isAfter(VALUES.getInternshipStartTime().minusMinutes(1))
                && time.isBefore(VALUES.getInternshipEndTime().plusMinutes(1));
    }

    public Set<String> validateVacationDate(LocalDate date) {
        Set<String> vacationMessagesSet = new HashSet<>();

        if (date.isBefore(LocalDate.now().plusDays(1))) {
            vacationMessagesSet.add(messages.dateIsBeforeToday());
        }

        if ("SUNDAY".equals(date.getDayOfWeek().toString()) || "SATURDAY".equals(date.getDayOfWeek().toString())) {
            vacationMessagesSet.add(messages.weekend());
        }

        if (date.isBefore(VALUES.getInternshipStartDate())) {
            vacationMessagesSet.add(messages.dateToEarly());
        }

        if (date.isAfter(VALUES.getInternshipEndDate())) {
            vacationMessagesSet.add(messages.dateTooLate());
        }
        return vacationMessagesSet;
    }

    public Set<String> validatePersonalDetails(LocalDate date, LocalTime startTime, LocalTime endTime, Long githubID, String githubName) {
        Set<Vacation> vacationsOnSameDay = getVacationsOnSameDay(date, githubID, githubName);
        int remainingVacation = calcRemainingVacationFromStudent(githubID, githubName);
        Set<String> vacationMessagesSet = new HashSet<>();
        long vacationRequestInMinutes = timeDifferenceInMinutes(startTime, endTime);

        if (vacationIsOverlapping(startTime, endTime, vacationsOnSameDay)) {
            vacationMessagesSet.add(messages.overlapping());
        }

        if (tooManyVacationsOnADay(vacationsOnSameDay)) {
            vacationMessagesSet.add(messages.tooManyVacations());
        }

        if (!vacationLongEnough(startTime, endTime)) {
            vacationMessagesSet.add(messages.vacationTooShort());
        }

        if (timeQuotaUsedUp(startTime, endTime, remainingVacation)) {
            vacationMessagesSet.add(messages.overTimeLimit());
        }

        if (vacationPresent(vacationsOnSameDay)
                && sumVacationsInMinutes(vacationsOnSameDay) + vacationRequestInMinutes > VALUES.getMaxHolidayBlockLength()
                && vacationRequestInMinutes + sumVacationsInMinutes(vacationsOnSameDay) < VALUES.getTotalHolidayTime()
        ) {
            vacationMessagesSet.add(messages.tooMuchVacationOnADay());
        }

        if (vacationRequestInMinutes > VALUES.getMaxHolidayBlockLength()
                && vacationRequestInMinutes < VALUES.getTotalHolidayTime()) {
            vacationMessagesSet.add(messages.tooMuchVacationOnADay());
        }


        if (vacationsOnSameDay.size() == 1 &&
                !(
                        (vacationsOnSameDay.iterator().next().getStartTime().equals(VALUES.getInternshipStartTime()) && endTime.equals(VALUES.getInternshipEndTime())) ||
                                (vacationsOnSameDay.iterator().next().getEndTime().equals(VALUES.getInternshipEndTime()) && startTime.equals(VALUES.getInternshipStartTime()))
                )
        ) {
            vacationMessagesSet.add(messages.wrongStartEndTimes());
        }
        return vacationMessagesSet;
    }

    private long sumVacationsInMinutes(Set<Vacation> vacationsOnSameDay) {
        return vacationsOnSameDay.stream().mapToLong(Vacation::getDuration).sum();
    }

    private boolean vacationPresent(Set<Vacation> vacationsOnSameDay) {
        return !vacationsOnSameDay.isEmpty();
    }


    private boolean tooManyVacationsOnADay(Set<Vacation> vacationsOnSameDay) {
        return vacationsOnSameDay.size() >= VALUES.getMaxNumberVacationOneDay();
    }

    public boolean timeIsOverlapping(LocalTime startExistingVacation, LocalTime endExistingVacation, LocalTime startNewVacation, LocalTime endNewVacation) {
        return ((startNewVacation.isAfter(startExistingVacation.minusMinutes(1)) && startNewVacation.isBefore(endExistingVacation.plusMinutes(1)))) ||
                (endNewVacation.isAfter(startExistingVacation.minusMinutes(1)) && endNewVacation.isBefore(endExistingVacation.plusMinutes(1))) ||
                (endNewVacation.isAfter(endExistingVacation.minusMinutes(1)) && startNewVacation.isBefore(startExistingVacation.plusMinutes(1)));
    }

    public boolean vacationIsOverlapping(LocalTime startNewVacation, LocalTime endNewVacation, Set<Vacation> vacationsOnSameDay) {
        return vacationsOnSameDay.stream()
                .anyMatch(vacation -> timeIsOverlapping(
                        vacation.getStartTime(), vacation.getEndTime(), startNewVacation, endNewVacation
                ));
    }


    private long timeDifferenceInMinutes(LocalTime startTime, LocalTime endTime) {
        return MINUTES.between(startTime, endTime);
    }


    private boolean timeQuotaUsedUp(LocalTime from, LocalTime to, int remainingVacation) {
        return timeDifferenceInMinutes(from, to) > remainingVacation;
    }

    private boolean vacationLongEnough(LocalTime from, LocalTime to) {
        return timeDifferenceInMinutes(from, to) >= VALUES.getMinMinutes();
    }

    //Testing
    public void removeAll() {
        studentRepository.removeAll();
    }

    public Student getStudent(Long githubID, String githubName) {
        return studentRepository.findStudent(githubID, githubName);
    }

    public void addStudent(Long githubID, String githubName) {
        Student student = new Student(githubID, githubName, new HashSet<>(), new HashSet<>());
        studentRepository.save(student);
    }

    public Set<ExamParticipation> getExamParticipationsFromStudent(Long githubID, String githubName) {
        return studentRepository.findStudent(githubID, githubName).getExam_participation();
    }

    public void addExamParticipation(Long githubID, Exam exam, String githubName) {
        freeVacationForExam(githubID, exam, githubName);

        Student student = studentRepository.findStudent(githubID, githubName);
        student.saveExamParticipation(exam.getLsf_id());
        studentRepository.save(student);
    }

    private void freeVacationForExam(Long githubID, Exam exam, String githubName) {
        LocalTime startTime = VALUES.getInternshipStartTime();
        LocalTime endTime = VALUES.getInternshipEndTime();
        int bonusTimeOnline = VALUES.getBonusTimeOnline();
        int bonusTimeOffline = VALUES.getBonusTimeOffline();
        Set<Vacation> vacationsOnSameDayAsExam = getVacationsOnSameDay(exam.getExam_date(), githubID, githubName);
        for (Vacation v :
                vacationsOnSameDayAsExam) {
            boolean vacationBeforeExam = v.getStartTime().isBefore(exam.getStartExemption(startTime, bonusTimeOffline, bonusTimeOnline));
            boolean vacationAfterExam = v.getEndTime().isAfter(exam.getEndExemption(endTime, bonusTimeOffline));

            boolean vacationStartsWithinExam = v.getStartTime().isAfter(exam.getStartExemption(startTime, bonusTimeOffline, bonusTimeOnline).minusMinutes(1)) && v.getStartTime().isBefore(exam.getEndExemption(endTime, bonusTimeOffline).plusMinutes(1));
            boolean vacationEndsWithinExam = v.getEndTime().isBefore(exam.getEndExemption(endTime, bonusTimeOffline).plusMinutes(1)) && v.getEndTime().isAfter(exam.getStartExemption(startTime, bonusTimeOffline, bonusTimeOnline).minusMinutes(1));

            if (vacationBeforeExam && vacationAfterExam) {
                removeVacationFromStudent(v.getVacation_date(), v.getStartTime(), v.getEndTime(), githubID, githubName);
                addVacation(v.getVacation_date(), v.getStartTime(), exam.getStartExemption(startTime, bonusTimeOffline, bonusTimeOnline), githubID, getStudent(githubID, githubName).getGithub_name());
                addVacation(v.getVacation_date(), exam.getEndExemption(endTime, bonusTimeOffline), v.getEndTime(), githubID, getStudent(githubID, githubName).getGithub_name());
            } else if (vacationBeforeExam && vacationEndsWithinExam) {
                removeVacationFromStudent(v.getVacation_date(), v.getStartTime(), v.getEndTime(), githubID, githubName);
                addVacation(v.getVacation_date(), v.getStartTime(), exam.getStartExemption(startTime, bonusTimeOffline, bonusTimeOnline), githubID, getStudent(githubID, githubName).getGithub_name());
            } else if (vacationStartsWithinExam && vacationAfterExam) {
                removeVacationFromStudent(v.getVacation_date(), v.getStartTime(), v.getEndTime(), githubID, githubName);
                addVacation(v.getVacation_date(), exam.getEndExemption(endTime, bonusTimeOffline), v.getEndTime(), githubID, getStudent(githubID, githubName).getGithub_name());
            } else if (vacationStartsWithinExam && vacationEndsWithinExam) {
                removeVacationFromStudent(v.getVacation_date(), v.getStartTime(), v.getEndTime(), githubID, githubName);
            }
        }
    }

    public void removeExamParticipationFromStudent(LocalDate date, Long lsfID, Long githubID, String githubName) {
        Student student = studentRepository.findStudent(githubID, githubName);
        boolean examParticipationExists = examParticipationExists(lsfID, githubID, githubName);
        boolean dateAfterToday = date.isAfter(LocalDate.now());
        boolean examExistsAndIsAfterToday = examParticipationExists && dateAfterToday;
        if (examExistsAndIsAfterToday) {
            student.removeExamParticipation(lsfID);
            studentRepository.save(student);
        }
    }

    private boolean examParticipationExists(Long lsfID, Long githubID, String githubName) {
        return getExamParticipationsFromStudent(githubID, githubName).stream()
                .anyMatch(examParticipation -> examParticipation.getLsfID().equals(lsfID));
    }

    public void refreshExams(LocalDate date, LocalTime from, LocalTime to, Long githubID, String githubName, List<Exam> examListByDate) {
        for (Exam e : examListByDate) {
            freeVacationForExam(githubID, e, githubName);
        }
    }

    public HashMap<Long, Long> getAllExamInfos() {

        HashMap<Long, Long> examInfos = new HashMap<>();
        List<Student> students = studentRepository.getStudents();
        students.forEach(student -> {
            Set<ExamParticipation> examParticipations = student.getExam_participation();
            examParticipations.forEach(examParticipation -> {
                if (examInfos.containsKey(examParticipation.getLsfID())) {
                    long numberOfStudents = examInfos.get(examParticipation.getLsfID());
                    examInfos.put(examParticipation.getLsfID(), numberOfStudents + 1L);
                } else {
                    examInfos.put(examParticipation.getLsfID(), 1L);
                }
            });

        });
        return examInfos;
    }

    public void removeVacationsIfNoMoreParticipations(Long githubID, String githubName, Set<Exam> sameDayExams, LocalDate examDate) {
        int counter = 0;
        for (Exam e : sameDayExams) {
            if (examParticipationExists(e.getLsf_id(), githubID, githubName)) {
                counter++;
            }

        }
        Set<Vacation> vacationsOnNoExamDay = getVacationsOnSameDay(examDate, githubID, githubName);
        if (counter == 0) {
            for (Vacation v : vacationsOnNoExamDay) {
                removeVacationFromStudent(examDate, v.getStartTime(), v.getEndTime(), githubID, githubName);
            }
        }
    }

    public HashMap<String, Set<String>> studentRegisteredToExam(Long githubID, String githubName, Exam exam) {
        HashMap<String, Set<String>> registerMessages = new HashMap<>();
        if(examParticipationExists(exam.getLsf_id(), githubID, githubName)){
            String registerMessage = messages.alreadyRegistered(exam.getExam_name(), exam.getLsf_id());
            registerMessages.put("registerError", Set.of(registerMessage));
        }
        return registerMessages;
    }
}
