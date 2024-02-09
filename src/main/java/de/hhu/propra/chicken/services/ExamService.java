package de.hhu.propra.chicken.services;

import de.hhu.propra.chicken.domain.model.Exam;
import de.hhu.propra.chicken.domain.model.ExamParticipation;
import de.hhu.propra.chicken.domain.service.ExamRepository;
import de.hhu.propra.chicken.dto.ExamInfo;
import de.hhu.propra.chicken.services.messages.Messages;
import de.hhu.propra.chicken.services.values.PropertiesValues;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class ExamService {

    private final PropertiesValues VALUES;
    private final ExamRepository examRepository;
    private final Messages messages;

    public ExamService(PropertiesValues propertiesValues, ExamRepository examRepository, Messages messages) {
        this.VALUES = propertiesValues;
        this.examRepository = examRepository;
        this.messages = messages;
    }

    public boolean ifLsfIDExistsInRepo(Long lsfID) {
        return examRepository.findAll().stream().anyMatch(exam -> lsfID.equals(exam.getLsf_id()));
    }

    public Exam examByLsfID(Long lsfID) {
        Optional<Exam> exam = examRepository.findAll().stream().filter(e -> lsfID.equals(e.getLsf_id())).findFirst();
        return exam.orElse(null);
    }

    public void addExam(Exam exam) {
        examRepository.save(exam);
    }

    public String lsfIDAvailable(Long lsfID) {
        String url = ("https://lsf.hhu.de/qisserver/rds?state=verpublish" +
                "&status=init&vmfile=no&publishid=%s&moduleCall=webInfo" +
                "&publishConfFile=webInfo&publishSubDir=veranstaltung").formatted(lsfID);
        try {
            Document docCustomConn = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(5000)
                    .cookie("cookiename", "ilovejsoup")
                    .referrer("http://google.com")
                    .header("headersecurity", "iamchicken")
                    .get();
            Element form = docCustomConn.select(".form.form").first();

            if (form.text().contains(String.valueOf(lsfID))){
                return "";
            }else{
                return messages.lsfIdNotAvailable();
            }
        } catch (Exception e) {
            return messages.anErrorOccurred();
        }
    }

    public HashMap<String, Set<String>> validateExam(Long lsfID, LocalDate date, LocalTime from, LocalTime to) {
        HashMap<String, Set<String>> messages = new HashMap<>();
        Set<String> dateMessages = validateExamDate(date);
        Set<String> startTimeMessages = validateExamStartTime(from);
        Set<String> endTimeMessages = validateExamEndTime(to);
        Set<String> lsfIDMessages = validateLsfID(lsfID, date);
        if (dateMessages.isEmpty() && startTimeMessages.isEmpty() && endTimeMessages.isEmpty() && lsfIDMessages.isEmpty()) {
            return new HashMap<>();
        } else {
            messages.put("dateErrors", dateMessages);
            messages.put("startErrors", startTimeMessages);
            messages.put("endErrors", endTimeMessages);
            messages.put("lsfIdErrors", lsfIDMessages);
            return messages;
        }
    }

    public Set<String> validateExamStartTime(LocalTime startTime) {
        Set<String> examMessages = new HashSet<>();
        if (!wholeQuarterHour(startTime.getMinute())) {
            examMessages.add(messages.wrongBlockStart());
        }
        return examMessages;
    }


    public Set<String> validateExamEndTime(LocalTime endTime) {
        Set<String> examMessages = new HashSet<>();
        if (!wholeQuarterHour(endTime.getMinute())) {
            examMessages.add(messages.wrongBlockEnd());
        }
        return examMessages;
    }

    private boolean wholeQuarterHour(int minutes) {
        return minutes % VALUES.getMinMinutes() == 0;
    }

    public Set<String> validateExamDate(LocalDate date) {
        Set<String> examMessagesSet = new HashSet<>();

        if (date.isBefore(LocalDate.now().plusDays(1))) {
            examMessagesSet.add(messages.examisBeforeToday());
        }

        if ("SUNDAY".equals(date.getDayOfWeek().toString())) {
            examMessagesSet.add(messages.weekend());
        }

        if (date.isBefore(VALUES.getInternshipStartDate())) {
            examMessagesSet.add(messages.dateToEarly());
        }

        if (date.isAfter(VALUES.getInternshipEndDate())) {
            examMessagesSet.add(messages.dateTooLate());
        }
        return examMessagesSet;
    }

    public Set<String> validateLsfID(Long lsfID, @DateTimeFormat LocalDate date) {
        Set<String> examMessages = new HashSet<>();
        String lsfIdMessage = lsfIDAvailable(lsfID);
        if (ifLsfIDExistsInRepo(lsfID)){
            examMessages.add(messages.examAlreadyExists(lsfID, date));
        } else if (!lsfIdMessage.isEmpty()){
            examMessages.add(lsfIdMessage);
        }
        return examMessages;
    }

    public List<ExamInfo> getAllExamsInfo() {
        LocalTime startTime = VALUES.getInternshipStartTime();
        LocalTime endTime = VALUES.getInternshipEndTime();
        int bonusTimeOnline = VALUES.getBonusTimeOnline();
        int bonusTimeOffline = VALUES.getBonusTimeOffline();
        return examRepository.findAll().stream()
                .map(exam -> new ExamInfo(
                        exam.getLsf_id(),
                        exam.getExam_name(),
                        exam.getExam_date(),
                        exam.getExam_start(),
                        exam.getExam_end(),
                        exam.isOffline(),
                        exam.getStartExemption(startTime, bonusTimeOffline, bonusTimeOnline),
                        exam.getEndExemption(endTime, bonusTimeOffline),
                        exam.isCancelable()))
                .collect(Collectors.toList());
    }

    public ExamInfo getExamsInfoLsfID(Long lsfID) {
        LocalTime startTime = VALUES.getInternshipStartTime();
        LocalTime endTime = VALUES.getInternshipEndTime();
        return examRepository.findAll().stream()
                .filter(exam -> lsfID.equals(exam.getLsf_id()))
                .map(exam -> new ExamInfo(
                        exam.getLsf_id(),
                        exam.getExam_name(),
                        exam.getExam_date(),
                        exam.getExam_start(),
                        exam.getExam_end(),
                        exam.isOffline(),
                        exam.getStartExemption(startTime, VALUES.getBonusTimeOffline(), VALUES.getBonusTimeOnline()),
                        exam.getEndExemption(endTime, VALUES.getBonusTimeOffline()),
                        exam.isCancelable()))
                .findAny()
                .orElse(null);
    }

    public void removeAll() {
        examRepository.removeAll();
    }

    public List<ExamInfo> getExamInfoFrom(Set<ExamParticipation> examParticipations) {
        LocalTime startTime = VALUES.getInternshipStartTime();
        LocalTime endTime = VALUES.getInternshipEndTime();
        List<Exam> exams = new ArrayList<>();
        for (ExamParticipation examParticipation : examParticipations) {
            if (ifLsfIDExistsInRepo(examParticipation.getLsfID())) {
                exams.add(examByLsfID(examParticipation.getLsfID()));
            }
        }
        return exams.stream().map(exam -> new ExamInfo(
                exam.getLsf_id(),
                exam.getExam_name(),
                exam.getExam_date(),
                exam.getExam_start(),
                exam.getExam_end(),
                exam.isOffline(),
                exam.getStartExemption(startTime, VALUES.getBonusTimeOffline(), VALUES.getBonusTimeOnline()),
                exam.getEndExemption(endTime, VALUES.getBonusTimeOffline()),
                exam.isCancelable())).toList();
    }

    public LocalDate getExamDateByLsfID(Long lsfID) {
        Exam examByLsfID = examByLsfID(lsfID);
        return examByLsfID.getExam_date();
    }

    public Exam getExamByLsfID(Long lsfID) {
        Exam exam = examByLsfID(lsfID);
        return exam;
    }

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public List<Exam> getExamsByParticipationsAndDate(Set<ExamParticipation> examParticipationsByGithubID, LocalDate date) {
        List<Exam> examList = new ArrayList<Exam>();
        for (ExamParticipation e : examParticipationsByGithubID) {
            examList.add(getExamByLsfID(e.getLsfID()));
        }
        return examList.stream().filter(e -> date.equals(e.getExam_date())).collect(Collectors.toList());
    }



    public List<Exam> getExamsFrom(Set<ExamParticipation> examParticipations) {
        List<Exam> exams = new ArrayList<>();
        for (ExamParticipation examParticipation : examParticipations) {
            if (ifLsfIDExistsInRepo(examParticipation.getLsfID())) {
                exams.add(examByLsfID(examParticipation.getLsfID()));
            }
        }
        return exams;
    }

    public Set<Exam> getExamsByDate(LocalDate examDateByLsfID) {
        return examRepository.findAll().stream().filter(e -> examDateByLsfID.equals(e.getExam_date())).collect(Collectors.toSet());
    }
}

