package de.hhu.propra.chicken.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Table("LOG")
public class Log {
    @Id
    @Column("LOG_ID")
    private Long log_id;
    @Column("GITHUB_ID")
    private final Long githubID;
    @Column("DESCRIPTION")
    private final String description;
    @Column("LOG_DATE")
    private final LocalDateTime dateTime;
    private final Set<Message> messages;

    public Log(Long githubID, String description, LocalDateTime dateTime, Set<Message> messages) {
        this.githubID = githubID;
        this.description = description;
        this.messages = new HashSet<>(messages);
        this.dateTime = dateTime;
    }

    public Long getGithubID() {
        return githubID;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public String toString() {
        return "Log{" +
                "githubID=" + githubID +
                ", description='" + description + '\'' +
                ", errorMessages=" + messages +
                ", dateTime=" + dateTime +
                '}';
    }

    public Set<Message> getErrorMessages() {
        return Set.copyOf(messages);
    }
}
