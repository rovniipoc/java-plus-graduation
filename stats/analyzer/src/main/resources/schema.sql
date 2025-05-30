CREATE TABLE IF NOT EXISTS similarities
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_a_id BIGINT        NOT NULL,
    event_b_id BIGINT        NOT NULL,
    score      DECIMAL(6, 5) NOT NULL,
    timestamp  TIMESTAMP     NOT NULL,
    CONSTRAINT uq_event_a_event_b UNIQUE (event_a_id, event_b_id)
);

CREATE TABLE IF NOT EXISTS weights
(
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id   BIGINT        NOT NULL,
    event_id  BIGINT        NOT NULL,
    weight    DECIMAL(6, 5) NOT NULL,
    timestamp TIMESTAMP     NOT NULL,
    CONSTRAINT uq_user_event UNIQUE (user_id, event_id)
);