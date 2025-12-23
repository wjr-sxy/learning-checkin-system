
CREATE TABLE IF NOT EXISTS sys_comment_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    category VARCHAR(50) DEFAULT 'General',
    usage_count INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_teacher (teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_task_submission_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    score INT,
    comment TEXT,
    grader_id BIGINT,
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    previous_score INT,
    previous_comment TEXT,
    INDEX idx_submission (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE sys_task_submission ADD COLUMN IF NOT EXISTS similarity_score DECIMAL(5,2) DEFAULT 0.00;
