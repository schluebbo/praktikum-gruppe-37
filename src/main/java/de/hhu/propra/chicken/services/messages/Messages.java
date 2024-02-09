package de.hhu.propra.chicken.services.messages;

import de.hhu.propra.chicken.services.values.PropertiesValues;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class Messages {

    private final PropertiesValues propertiesValues;

    public Messages(PropertiesValues propertiesValues) {
        this.propertiesValues = propertiesValues;
    }

    public String dateToEarly() {
        return "Das Datum ist vor dem ersten Praktikumstag am %s.".formatted(propertiesValues.getInternshipStartDate());
    }

    public String timeToEarly() {
        return "Die früheste erlaubte Startzeit für Sie ist: %s Uhr.".formatted(propertiesValues.getInternshipStartTime());
    }

    public String timeTooLate() {
        return "Die späteste erlaubte Endzeit für Sie ist: %s.".formatted(propertiesValues.getInternshipEndTime());
    }

    public String examisBeforeToday() {
        return "Sie können Klausuren nur für die Folgetage anmelden. Bitte melden Sie sich bei den Tutor:innen oder Organisator:innen, wenn Sie heute noch Klausur benötigen oder Klausur nachmelden wollen";
    }

    public String wrongBlockStart() {
        return "Die Startzeiten muss eine ganze Viertelstunden sein.";
    }

    public String wrongBlockEnd() {
        return "Die Endzeit muss eine ganze Viertelstunden sein.";
    }

    public String wrongStartEndTimes() {
        return "Falsche Start und Endzeit.";
    }

    public String dateTooLate() {
        return "Das Datum ist nach dem letzten Praktikumstag am %s.".formatted(propertiesValues.getInternshipEndDate());
    }

    public String weekend() {
        return "Das Datum liegt am Wochenende.";
    }

    public String dateIsBeforeToday() {
        return "Sie können Urlaube nur für die Folgetage anmelden. Bitte melden Sie sich bei den Tutor:innen oder Organisator:innen, wenn Sie heute noch Urlaub benötigen oder Urlaub nachmelden wollen";
    }

    public String overlapping() {
        return "Urlaubsblöcke überschneiden sich, bitte stornieren Sie den überlappenden Urlaub.";
    }

    public String tooManyVacations() {
        return "An einem Tag können maximal %s Urlaube genommen werden.".formatted(propertiesValues.getMaxNumberVacationOneDay());
    }

    public String vacationTooShort() {
        return "Urlaub darf nicht weniger als %s Minuten betragen.".formatted(propertiesValues.getMinMinutes());
    }

    public String overTimeLimit() {
        return "Verfügbare Urlaubszeit wurde überschritten.";
    }

    public String tooMuchVacationOnADay() {
        return "Sie können entweder den gesamten Tag frei nehmen, oder bis zu %s Minuten.".formatted(propertiesValues.getMaxHolidayBlockLength());
    }

    public String lsfIdNotAvailable() {
        return "Angegebene LSF Veranstaltungs-ID kann nicht gefunden werden";
    }

    public String examAlreadyExists(Long lsfID, LocalDate date) {
        return "Klausur mit der LSF-ID %s am %s existiert bereits.".formatted(lsfID, date);
    }

    public String timeTooEarlyExam() {
        return "Die früheste erlaubte Startzeit für Sie ist: %s Uhr.".formatted(propertiesValues.getExamStartTime());
    }

    public String timeTooLateExam() {
        return "Die späteste erlaubte Endzeit für Sie ist: %s.".formatted(propertiesValues.getExamEndTime());
    }

    public String anErrorOccurred() {
        return "Beim Abfragen der LSF-ID ist ein Fehler aufgetreten. " +
                "Bitte versuchen sie es erneut oder " +
                "schreiben sie eine Mail an <a href=\"mailto:test@hhu.de\">test@hhu.de</a>";
    }

    public String alreadyRegistered(String examName, Long lsfID) {
        return "Sie sind bereits für die Klausur %s mit der LSF-ID: %s registriert".formatted(examName, lsfID);
    }
}
