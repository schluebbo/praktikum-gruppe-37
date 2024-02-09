DROP TABLE IF EXISTS VACATION;
DROP TABLE IF EXISTS EXAM_PARTICIPATION;
DROP TABLE IF EXISTS STUDENT;
DROP TABLE IF EXISTS EXAM;


CREATE TABLE IF NOT EXISTS EXAM
(
    EXAM_VERSION INT,
    LSF_ID       INT PRIMARY KEY,
    EXAM_DATE    DATE,
    EXAM_START   TIME(0),
    EXAM_END     TIME(0),
    EXAM_NAME    TEXT,
    EXAM_OFFLINE BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS STUDENT
(
    STUDENT_VERSION INT,
    GITHUB_ID       INT PRIMARY KEY,
    GITHUB_NAME     VARCHAR(40)
);

CREATE TABLE IF NOT EXISTS EXAM_PARTICIPATION
(
    STUDENT INT,
    LSF_ID  INT,
    PRIMARY KEY (STUDENT, LSF_ID),
    FOREIGN KEY (STUDENT) REFERENCES STUDENT (GITHUB_ID),
    FOREIGN KEY (LSF_ID) REFERENCES EXAM (LSF_ID)
);

CREATE TABLE IF NOT EXISTS VACATION
(
    STUDENT       INT,
    GITHUB_ID     INT,
    FOREIGN KEY (GITHUB_ID) REFERENCES STUDENT (GITHUB_ID),
    VACATION_DATE DATE,
    START_TIME    TIME,
    END_TIME      TIME
);
