CREATE TABLE T_HH_RAW_VACANCIES
(
    ID serial NOT NULL,
    URL character varying(256) NOT NULL,
    DATA jsonb NOT NULL,
    DATE_CREATE timestamp without time zone NOT NULL,
    SEQUENCE_NUM integer NOT NULL
);

CREATE TABLE T_HH_RAW_RESUMES
(
    ID serial NOT NULL,
    URL character varying(256) NOT NULL,
    DATA jsonb NOT NULL,
    DATE_CREATE timestamp without time zone NOT NULL,
    SEQUENCE_NUM integer NOT NULL
);