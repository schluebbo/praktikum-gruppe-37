package de.hhu.propra.chicken.domain.model;


import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


//@SecondaryTable(name="rb_user_password", pkJoinColumns=@PrimaryKeyJoinColumn(name="user_id"))
@Table("EXAM_PARTICIPATION")
public final class ExamParticipation {

    @Column("LSF_ID")
    private final Long lsfID;

    public ExamParticipation(Long lsfID) {
        this.lsfID = lsfID;
    }

    public Long getLsfID() {
        return lsfID;
    }
}
