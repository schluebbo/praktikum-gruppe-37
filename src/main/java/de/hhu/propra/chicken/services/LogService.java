package de.hhu.propra.chicken.services;

import de.hhu.propra.chicken.domain.model.Message;
import de.hhu.propra.chicken.domain.model.Log;
import de.hhu.propra.chicken.domain.service.LogRepository;
import de.hhu.propra.chicken.services.values.Task;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressFBWarnings("EI_EXPOSE_REP2")
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public List<Log> getAllLogInfos(){
        List<Log> logInfos = new ArrayList<>();
        List<Log> logRepositoryAll = logRepository.findAll();
        for (Log log: logRepositoryAll) {
            Set<Message> messages = log.getErrorMessages().stream()
                    .map(message -> new Message(message.getErrorDescription()))
                    .collect(Collectors.toSet());
            Log logInfo = new Log(log.getGithubID(), log.getDescription(), log.getDateTime(), messages);
            logInfos.add(logInfo);
        }
        return logInfos;
    }

    public void logError(Task task, Long githubID, String githubName, LocalDate date, LocalTime startTime, LocalTime endTime, HashMap<String, Set<String>> messages) {
        String descipction;
        Log log = null;
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Set<Message> collect = messages.values().stream()
                .flatMap(Collection::stream)
                .map(Message::new)
                .collect(Collectors.toSet());

        switch (task) {
            case VACATION_REGISTRATION -> {
                descipction = "[GithubID: %s, Datum: %s] %s: konnte für den %s, %s - %s keinen Urlaub nehmen.".formatted(githubID, now, githubName, date, startTime, endTime);
                log = new Log(githubID, descipction, now, collect);
            }
            case CREATE_EXAM -> {
                descipction = "[GithubID: %s, Datum: %s] %s: konnte für den %s, %s - %s keine Klausur erstellen.".formatted(githubID, now, githubName, date, startTime, endTime);
                log = new Log(githubID, descipction, now, collect);
            }
            case EXAM_REGISTRATION -> {
                descipction = "[GithubID: %s, Datum: %s] %s konnte sich für den %s, %s - %s  nicht für die Klausur anmeldem.".formatted(githubID, now, githubName, date, startTime, endTime);
                log = new Log(githubID, descipction, now, new LinkedHashSet<>());
            }
            default -> {
                descipction = "[GithubID: %s, Datum: %s] %s hat einen Fehler produziert".formatted(githubID, now, githubName);
                log = new Log(githubID, descipction, now, new LinkedHashSet<>());
            }
        }
        logRepository.save(log);
    }

    public void logSuccess(Task task, Long githubID, String githubName, Object data, Object... data2){
        String descipction;
        Log log = null;
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Object data3 = data;
        for (Object o: data2) {
            data3 = data3+(", "+o);
        }


        switch (task) {
            case VACATION_REGISTRATION -> {
                descipction = "[GithubID: %s, Datum: %s] %s: hat für den %s Urlaub genommen.".formatted(githubID, now, githubName, data3);
                log = new Log(githubID, descipction, now, new LinkedHashSet<>());
            }
            case CREATE_EXAM -> {
                descipction = "[GithubID: %s, Datum: %s] %s: hat für den %s eine Klausur erstellen.".formatted(githubID, now, githubName, data3);
                log = new Log(githubID, descipction, now, new LinkedHashSet<>());
            }
            case CANCEL_VACATION -> {
                descipction = "[GithubID: %s, Datum: %s] %s hat für den %s Urlaub storniert.".formatted(githubID, now, githubName, data3);
                log = new Log(githubID, descipction, now, new LinkedHashSet<>());
            }
            case EXAM_REGISTRATION -> {
                descipction = "[GithubID: %s, Datum: %s] %s hat sich für eine Klausur LSF-ID: %s angemeldet.".formatted(githubID, now, githubName, data3);
                log = new Log(githubID, descipction, now, new LinkedHashSet<>());
            }
            case EXAM_SIGN_OUT -> {
                descipction = "[GithubID: %s, Datum: %s] %s hat sich von einer Klausur LSF-ID: %s abgemeldet.".formatted(githubID, now, githubName, data3);
                log = new Log(githubID, descipction, now, new LinkedHashSet<>());
            }
            default -> {
                descipction = "[GithubID: %s, Datum: %s] %s ".formatted(githubID, now, githubName);
                log = new Log(githubID, descipction, now, new LinkedHashSet<>());
            }
        }
        logRepository.save(log);
    }
}

