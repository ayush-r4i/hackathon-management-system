-- ============================================================
-- College Fest & Hackathon Management System - Schema
-- ============================================================
-- These tables are also created automatically by Hibernate
-- (spring.jpa.hibernate.ddl-auto=update) based on the @Entity
-- classes. This file is kept for clarity/learning and uses
-- "IF NOT EXISTS" so it never conflicts with what Hibernate creates.
-- ============================================================

CREATE TABLE IF NOT EXISTS admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description VARCHAR(1000),
    venue VARCHAR(150) NOT NULL,
    category VARCHAR(100),
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    max_team_size INT NOT NULL,
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(150) NOT NULL,
    event_id BIGINT NOT NULL,
    leader_name VARCHAR(100) NOT NULL,
    leader_email VARCHAR(100),
    leader_phone VARCHAR(20),
    registered_at DATETIME,
    CONSTRAINT fk_team_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS participant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    college VARCHAR(150),
    username VARCHAR(100) UNIQUE,
    password_hash VARCHAR(64),
    team_id BIGINT NOT NULL,
    CONSTRAINT fk_participant_team FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS judge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    expertise VARCHAR(150),
    username VARCHAR(100) UNIQUE,
    password_hash VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS judge_event_assignment (
    judge_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    PRIMARY KEY (judge_id, event_id),
    CONSTRAINT fk_jea_judge FOREIGN KEY (judge_id) REFERENCES judge (id) ON DELETE CASCADE,
    CONSTRAINT fk_jea_event FOREIGN KEY (event_id) REFERENCES event (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS score (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    judge_id BIGINT NOT NULL,
    innovation DOUBLE NOT NULL,
    technical_implementation DOUBLE NOT NULL,
    presentation DOUBLE NOT NULL,
    ui_ux DOUBLE NOT NULL,
    overall_impression DOUBLE NOT NULL,
    final_score DOUBLE,
    scored_at DATETIME,
    CONSTRAINT fk_score_team FOREIGN KEY (team_id) REFERENCES team (id) ON DELETE CASCADE,
    CONSTRAINT fk_score_judge FOREIGN KEY (judge_id) REFERENCES judge (id) ON DELETE CASCADE,
    CONSTRAINT uq_score_team_judge UNIQUE (team_id, judge_id)
);

-- ============================================================
-- Default admin login (idempotent - INSERT IGNORE relies on the
-- unique "username" column, so this never creates duplicates even
-- though schema.sql runs again every time the app starts).
-- Username: admin | Password: admin123
-- ============================================================
INSERT IGNORE INTO admin (username, password, full_name)
VALUES ('admin', 'admin123', 'Fest Administrator');
