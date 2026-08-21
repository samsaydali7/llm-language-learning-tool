-- Core knowledge base schema for the Language Learning Engine (SPEC.md / REQUIREMENTS.md V1).

CREATE TABLE language (
    id         BIGSERIAL PRIMARY KEY,
    code       VARCHAR(16)  NOT NULL,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uq_language_code UNIQUE (code)
);

CREATE TABLE book (
    id                        BIGSERIAL PRIMARY KEY,
    language_id               BIGINT       NOT NULL REFERENCES language (id),
    title                     VARCHAR(1024) NOT NULL,
    description               TEXT,
    explanation_language_code VARCHAR(16)  NOT NULL,
    pdf_path                  VARCHAR(1024),
    pdf_page_count            INTEGER,
    created_at                TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_book_language ON book (language_id);

CREATE TABLE structure_node (
    id          BIGSERIAL PRIMARY KEY,
    book_id     BIGINT       NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    parent_id   BIGINT       REFERENCES structure_node (id) ON DELETE CASCADE,
    node_type   VARCHAR(32)  NOT NULL,
    title       VARCHAR(1024) NOT NULL,
    order_index INTEGER      NOT NULL,
    start_page  INTEGER,
    end_page    INTEGER,
    created_at  TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_structure_node_book ON structure_node (book_id);
CREATE INDEX idx_structure_node_parent ON structure_node (parent_id);

CREATE TABLE audio_file (
    id                 BIGSERIAL PRIMARY KEY,
    book_id            BIGINT       NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    original_filename  VARCHAR(1024) NOT NULL,
    storage_path       VARCHAR(1024) NOT NULL,
    content_type       VARCHAR(255),
    created_at         TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_audio_file_book ON audio_file (book_id);

CREATE TABLE audio_reference (
    id                BIGSERIAL PRIMARY KEY,
    book_id           BIGINT      NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    structure_node_id BIGINT      REFERENCES structure_node (id) ON DELETE SET NULL,
    page              INTEGER,
    label             VARCHAR(255) NOT NULL,
    raw_context       TEXT,
    audio_file_id     BIGINT      REFERENCES audio_file (id) ON DELETE SET NULL,
    match_confidence  DOUBLE PRECISION,
    created_at        TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE INDEX idx_audio_reference_book ON audio_reference (book_id);
CREATE INDEX idx_audio_reference_audio_file ON audio_reference (audio_file_id);

CREATE TABLE topic (
    id          BIGSERIAL PRIMARY KEY,
    language_id BIGINT       NOT NULL REFERENCES language (id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uq_topic_language_name UNIQUE (language_id, name)
);

CREATE TABLE knowledge_item (
    id               BIGSERIAL PRIMARY KEY,
    item_type        VARCHAR(31)  NOT NULL,
    book_id          BIGINT       NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    structure_node_id BIGINT      REFERENCES structure_node (id) ON DELETE SET NULL,
    page             INTEGER,
    headword         TEXT         NOT NULL,
    summary          TEXT,
    notes            TEXT,
    source_excerpt   TEXT,
    part_of_speech   VARCHAR(255),
    pattern_text     TEXT,
    usage_notes      TEXT,
    created_at       TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_knowledge_item_book ON knowledge_item (book_id);
CREATE INDEX idx_knowledge_item_structure_node ON knowledge_item (structure_node_id);
CREATE INDEX idx_knowledge_item_type ON knowledge_item (item_type);

CREATE TABLE knowledge_item_topic (
    knowledge_item_id BIGINT NOT NULL REFERENCES knowledge_item (id) ON DELETE CASCADE,
    topic_id          BIGINT NOT NULL REFERENCES topic (id) ON DELETE CASCADE,
    PRIMARY KEY (knowledge_item_id, topic_id)
);
CREATE INDEX idx_knowledge_item_topic_topic ON knowledge_item_topic (topic_id);

CREATE TABLE knowledge_example (
    id                BIGSERIAL PRIMARY KEY,
    knowledge_item_id BIGINT    NOT NULL REFERENCES knowledge_item (id) ON DELETE CASCADE,
    example_text      TEXT      NOT NULL,
    translation       TEXT,
    page              INTEGER,
    created_at        TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_knowledge_example_item ON knowledge_example (knowledge_item_id);

CREATE TABLE extraction_job (
    id                BIGSERIAL PRIMARY KEY,
    book_id           BIGINT      NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    status            VARCHAR(32) NOT NULL,
    stage             VARCHAR(32) NOT NULL,
    total_sections    INTEGER,
    completed_sections INTEGER,
    error_message     TEXT,
    updated_at        TIMESTAMP,
    created_at        TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE INDEX idx_extraction_job_book ON extraction_job (book_id);

CREATE TABLE exercise_generation_job (
    id                  BIGSERIAL PRIMARY KEY,
    scope_json          TEXT        NOT NULL,
    exercise_types_json TEXT,
    exercise_count      INTEGER     NOT NULL,
    status              VARCHAR(32) NOT NULL,
    error_message       TEXT,
    updated_at          TIMESTAMP,
    created_at          TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE TABLE exercise (
    id             BIGSERIAL PRIMARY KEY,
    job_id         BIGINT      REFERENCES exercise_generation_job (id) ON DELETE SET NULL,
    book_id        BIGINT      NOT NULL REFERENCES book (id) ON DELETE CASCADE,
    exercise_type  VARCHAR(32) NOT NULL,
    prompt         TEXT        NOT NULL,
    options_json   TEXT,
    correct_answer TEXT        NOT NULL,
    explanation    TEXT,
    audio_file_id  BIGINT      REFERENCES audio_file (id) ON DELETE SET NULL,
    created_at     TIMESTAMP   NOT NULL DEFAULT now()
);
CREATE INDEX idx_exercise_job ON exercise (job_id);
CREATE INDEX idx_exercise_book ON exercise (book_id);

CREATE TABLE exercise_knowledge_item (
    exercise_id       BIGINT NOT NULL REFERENCES exercise (id) ON DELETE CASCADE,
    knowledge_item_id BIGINT NOT NULL REFERENCES knowledge_item (id) ON DELETE CASCADE,
    PRIMARY KEY (exercise_id, knowledge_item_id)
);
CREATE INDEX idx_exercise_knowledge_item_item ON exercise_knowledge_item (knowledge_item_id);

CREATE TABLE exercise_attempt (
    id               BIGSERIAL PRIMARY KEY,
    exercise_id      BIGINT    NOT NULL REFERENCES exercise (id) ON DELETE CASCADE,
    submitted_answer TEXT,
    is_correct       BOOLEAN   NOT NULL,
    attempted_at     TIMESTAMP NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_exercise_attempt_exercise ON exercise_attempt (exercise_id);

CREATE TABLE knowledge_review_state (
    id                BIGSERIAL PRIMARY KEY,
    knowledge_item_id BIGINT    NOT NULL REFERENCES knowledge_item (id) ON DELETE CASCADE,
    times_failed      INTEGER   NOT NULL DEFAULT 0,
    times_reviewed    INTEGER   NOT NULL DEFAULT 0,
    last_failed_at    TIMESTAMP,
    last_reviewed_at  TIMESTAMP,
    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_knowledge_review_state_item UNIQUE (knowledge_item_id)
);
