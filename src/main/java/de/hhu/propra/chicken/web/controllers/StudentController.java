package de.hhu.propra.chicken.web.controllers;

import de.hhu.propra.chicken.domain.model.Exam;
import de.hhu.propra.chicken.domain.model.ExamParticipation;
import de.hhu.propra.chicken.domain.model.Log;
import de.hhu.propra.chicken.domain.model.Vacation;
import de.hhu.propra.chicken.dto.ExamInfo;
import de.hhu.propra.chicken.dto.ExamParticipationInfo;
import de.hhu.propra.chicken.services.ExamService;
import de.hhu.propra.chicken.services.LogService;
import de.hhu.propra.chicken.services.StudentService;
import de.hhu.propra.chicken.web.forms.AddVacationForm;
import de.hhu.propra.chicken.web.forms.CancelVacationForm;
import de.hhu.propra.chicken.web.forms.CreateExamForm;
import de.hhu.propra.chicken.web.forms.RegistrationForm;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static de.hhu.propra.chicken.services.values.Task.*;

@Controller
@PropertySource("classpath:application.properties")
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class StudentController {

    private final ExamService examService;
    private final StudentService studentService;
    private final LogService logService;

    public StudentController(ExamService examService, StudentService studentService, LogService logService) {
        this.examService = examService;
        this.studentService = studentService;
        this.logService = logService;
    }

    @ModelAttribute("githubName")
    String handle(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return null;
        }
        return principal.getAttribute("login");
    }

    @ModelAttribute("githubID")
    Long githubID(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return null;
        }
        return Long.valueOf(principal.getAttributes().get("id").toString());
    }


    @GetMapping("/")
    public String index(@AuthenticationPrincipal OAuth2User oAuth2User) {
        if (oAuth2User == null) {
            return "redirect:/oauth2/authorization/github";
        }
        if (oAuth2User.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ORGANIZER"))) {
            return "redirect:/organizer";
        }
        if (oAuth2User.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_TUTOR"))) {
            return "redirect:/tutor";
        }
        if (oAuth2User.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_STUDENT"))) {
            return "redirect:/student";
        }
        return "redirect:https://i.kym-cdn.com/entries/icons/mobile/000/021/807/ig9OoyenpxqdCQyABmOQBZDI0duHk2QZZmWg2Hxd4ro.jpg";
    }

    @GetMapping("/student")
    public String student(@ModelAttribute("githubName") String githubName,
                          @ModelAttribute("githubID") Long githubID,
                          Model model) {

        if (studentService.getStudent(githubID, githubName) == null) {
            studentService.addStudent(githubID, githubName);
        }

        Set<ExamParticipation> examParticipation = studentService.getExamParticipationsFromStudent(githubID, githubName);
        List<ExamInfo> examInfos = examService.getExamInfoFrom(examParticipation);
        Set<Vacation> vacationsList = studentService.getVacationFromStudent(githubID, githubName);
        int vacationDuration = studentService.calcVacationDurationFromStudent(githubID, githubName);
        int remainingVacationTime = studentService.calcRemainingVacationFromStudent(githubID, githubName);

        model.addAttribute("user", githubName);
        model.addAttribute("vacations", vacationsList);
        model.addAttribute("vacationDuration", vacationDuration);
        model.addAttribute("remainingVacationTime", remainingVacationTime);
        model.addAttribute("examInfos", examInfos);
        return "student";
    }

    @GetMapping("/vacationRegistration")
    public String vacation() {
        return "vacation";
    }

    @PostMapping("/vacationRegistration")
    public String vacationRegistration(@ModelAttribute("githubName") String githubName,
                                       @ModelAttribute("githubID") Long githubID,
                                       @ModelAttribute("form") AddVacationForm form,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes) {

        Set<ExamParticipation> examParticipationsByGithubID = studentService.getExamParticipationsFromStudent(githubID, githubName);
        List<Exam> examListByDate = examService.getExamsByParticipationsAndDate(examParticipationsByGithubID, form.date());
        boolean hasExamOnDay = !examListByDate.isEmpty();
        HashMap<String, Set<String>> messages = studentService.validateVacation(form.date(), form.startTime(), form.endTime(), githubID, githubName, hasExamOnDay);

        if (messages.isEmpty() && !bindingResult.hasErrors()) {
            studentService.addVacation(form.date(), form.startTime(), form.endTime(), githubID, githubName);
            studentService.refreshExams(form.date(), form.startTime(), form.endTime(), githubID, githubName, examListByDate);
        } else {
            redirectAttributes.addFlashAttribute("messagesDate", messages.get("dateErrors"));
            redirectAttributes.addFlashAttribute("messagesStartTime", messages.get("startErrors"));
            redirectAttributes.addFlashAttribute("messagesEndTime", messages.get("endErrors"));
            redirectAttributes.addFlashAttribute("messages", messages.get("personalErrors"));
            logService.logError(VACATION_REGISTRATION, githubID, githubName, form.date(), form.startTime(), form.endTime(), messages);
            return "redirect:/vacationRegistration";
        }
        logService.logSuccess(VACATION_REGISTRATION, githubID, githubName, messages, form.date());
        return "redirect:/student";
    }

    @PostMapping("/cancelVacation")
    public String cancelVacation(@ModelAttribute("githubName") String githubName,
                                 @ModelAttribute("githubID") Long githubID,
                                 @ModelAttribute("cancelForm") CancelVacationForm form) {
        studentService.removeVacationFromStudent(form.date(), form.startTime(), form.endTime(), githubID, githubName);
        logService.logSuccess(CANCEL_VACATION, githubID, githubName, form.date());
        return "redirect:/student";
    }

    @GetMapping("/createExam")
    public String createExam(@AuthenticationPrincipal OAuth2User oAuth2User, Model model) {
        return "createExam";
    }

    @PostMapping("/createExam")
    public String createActualExam(@ModelAttribute("githubName") String githubName,
                                   @ModelAttribute("githubID") Long githubID,
                                   @Valid @ModelAttribute("examForm") CreateExamForm form,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes) {

        HashMap<String, Set<String>> messages = examService.validateExam(form.lsfID(), form.date(), form.startTime(), form.endTime());

        if (messages.isEmpty() && !bindingResult.hasErrors()) {
            examService.addExam(new Exam(form.date(), form.startTime(), form.endTime(), form.examName(), form.lsfID(), form.offline()));
        } else {
            redirectAttributes.addFlashAttribute("messageLsfID", messages.get("lsfIdErrors"));
            redirectAttributes.addFlashAttribute("messagesDate", messages.get("dateErrors"));
            redirectAttributes.addFlashAttribute("messagesStartTime", messages.get("startErrors"));
            redirectAttributes.addFlashAttribute("messagesEndTime", messages.get("endErrors"));
            logService.logError(CREATE_EXAM, githubID, githubName, form.date(), form.startTime(), form.endTime(), messages);
            return "redirect:/createExam";
        }
        logService.logSuccess(CREATE_EXAM, githubID, githubName, form.date(), form.examName());
        return "redirect:/examRegistration";
    }

    @GetMapping("/examRegistration")
    public String getExamRegistration(Model model) {
        model.addAttribute("exams", examService.getAllExamsInfo());
        return "examRegistration";
    }

    @PostMapping("/examRegistration")
    public String postExamRegistration(@ModelAttribute("githubName") String githubName,
                                       @ModelAttribute("githubID") Long githubID,
                                       @Valid @ModelAttribute("klausur_anmelden") RegistrationForm form,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes)  {
        Exam exam = examService.getExamByLsfID(form.lsfID());
        HashMap<String, Set<String>> message = studentService.studentRegisteredToExam(githubID, githubName, exam);

        if (message.isEmpty() && !bindingResult.hasErrors()){
            studentService.addExamParticipation(githubID, exam, githubName);
            logService.logSuccess(EXAM_REGISTRATION, githubID, githubName, form.lsfID());
        }else{
            redirectAttributes.addFlashAttribute("messages", message.get("registerError"));
            logService.logError(EXAM_REGISTRATION, githubID, githubName, exam.getExam_date(), exam.getExam_start(), exam.getExam_end(), message);
            return "redirect:/examRegistration";
        }
        return "redirect:/student";
    }

    @PostMapping("/examParticipation/{lsfID}/delete")
    public String cancelExamParticipation(@ModelAttribute("githubName") String githubName,
                                          @ModelAttribute("githubID") Long githubID,
                                          @PathVariable Long lsfID){

        LocalDate examDateByLsfID = examService.getExamDateByLsfID(lsfID);
        Set<Exam> examsByDate = examService.getExamsByDate(examDateByLsfID);

        studentService.removeExamParticipationFromStudent(examDateByLsfID, lsfID, githubID, githubName);
        studentService.removeVacationsIfNoMoreParticipations(githubID, githubName, examsByDate, examDateByLsfID);
        logService.logSuccess(EXAM_SIGN_OUT, githubID, githubName, lsfID);
        return "redirect:/student";
    }


    @Secured("ROLE_ORGANIZER")
    @GetMapping("/organizer")
    public String orgaIndex(Model model) {
        List<Log> logInfos = logService.getAllLogInfos();
        model.addAttribute("logInfos", logInfos);
        return "organizer";
    }

    @Secured("ROLE_TUTOR")
    @GetMapping("/tutor")
    public String tutorIndex(Model model) {
        List<Exam> allExams = examService.getAllExams();
        HashMap<Long, Long> allExamInfos = studentService.getAllExamInfos();
        List<ExamParticipationInfo> exams = allExams.stream().map(exam -> new ExamParticipationInfo(
                exam.getLsf_id(),
                exam.getExam_name(),
                exam.getExam_date(),
                exam.getExam_start(),
                exam.getExam_end(),
                exam.isOffline(),
                allExamInfos.get(exam.getLsf_id())
        )).toList();
        model.addAttribute("exams", exams);
        return "tutor";
    }

}
