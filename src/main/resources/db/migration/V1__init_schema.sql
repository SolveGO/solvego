CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       created_at DATETIME(6),
                       PRIMARY KEY (id),
                       CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE problems (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          creator_id BIGINT NOT NULL,
                          title VARCHAR(50) NOT NULL,
                          description VARCHAR(100) NOT NULL,
                          black_stones JSON NOT NULL,
                          white_stones JSON NOT NULL,
                          next_player VARCHAR(20) NOT NULL,
                          answer_x INT NOT NULL,
                          answer_y INT NOT NULL,
                          created_at DATETIME(6),
                          PRIMARY KEY (id),
                          CONSTRAINT fk_problems_creator
                              FOREIGN KEY (creator_id) REFERENCES users (id)
);

CREATE TABLE attempts (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          user_id BIGINT NOT NULL,
                          problem_id BIGINT NOT NULL,
                          selected_x INT NOT NULL,
                          selected_y INT NOT NULL,
                          is_correct BOOLEAN NOT NULL,
                          attempted_at DATETIME(6),
                          PRIMARY KEY (id),
                          CONSTRAINT fk_attempts_user
                              FOREIGN KEY (user_id) REFERENCES users (id),
                          CONSTRAINT fk_attempts_problem
                              FOREIGN KEY (problem_id) REFERENCES problems (id)
);