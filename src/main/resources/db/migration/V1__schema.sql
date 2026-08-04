CREATE TABLE doctor (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100)    NOT NULL,
    last_name  VARCHAR(100)    NOT NULL,
    timezone   VARCHAR(64)     NOT NULL
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE patient (
    id         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100)    NOT NULL,
    last_name  VARCHAR(100)    NOT NULL,
    KEY idx_patient_first_last (first_name, last_name, id),
    KEY idx_patient_last_first (last_name, first_name, id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE visit (
    id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT UNSIGNED NOT NULL,
    doctor_id       BIGINT UNSIGNED NOT NULL,
    start_date_time DATETIME(0)     NOT NULL,
    end_date_time   DATETIME(0)     NOT NULL,
    CONSTRAINT fk_visit_patient FOREIGN KEY (patient_id) REFERENCES patient (id),
    CONSTRAINT fk_visit_doctor FOREIGN KEY (doctor_id) REFERENCES doctor (id),
    KEY idx_visit_doctor_time (doctor_id, start_date_time, end_date_time),
    KEY idx_visit_patient_doctor_start (patient_id, doctor_id, start_date_time DESC, end_date_time),
    KEY idx_visit_doctor_patient (doctor_id, patient_id)
) DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
