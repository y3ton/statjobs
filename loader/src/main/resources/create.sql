CREATE TABLE T_QUEUE_DOWNLOADABLE_LINK
(
    ID serial NOT NULL,
    HANDLER_NAME character varying NOT NULL,
    URL character varying NOT NULL,
    DATE_CREATE timestamp without time zone NOT NULL,
    DATE_PROCESS timestamp without time zone,
    SEQUENCE_NUM integer NOT NULL,
    IS_DELETE boolean NOT NULL
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

CREATE TABLE T_HH_RAW_VACANCIES
(
    ID serial NOT NULL,
    URL character varying(256) NOT NULL,
    DATA jsonb NOT NULL,
    DATE_CREATE timestamp without time zone NOT NULL,
    SEQUENCE_NUM integer NOT NULL
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;