package de.hhu.propra.chicken.domain.model;

import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("ERROR_MESSAGE")
public class Message {

    @Column("ERROR_DESCRIPTION")
    private final String errorDescription;

    public Message(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getErrorDescription() {
        return errorDescription;
    }
}
