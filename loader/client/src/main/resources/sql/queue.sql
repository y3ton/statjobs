CREATE TABLE T_QUEUE_DOWNLOADABLE_LINK
(
    ID serial NOT NULL,
    HANDLER_NAME character varying NOT NULL,
    URL character varying NOT NULL,
    DATE_CREATE timestamp without time zone NOT NULL,
    DATE_PROCESS timestamp without time zone,
    SEQUENCE_NUM integer NOT NULL,
    IS_DELETE boolean NOT NULL,
    PROPS jsonb,
    CONSTRAINT SEQ_URL_KEY UNIQUE (SEQUENCE_NUM, URL)
)