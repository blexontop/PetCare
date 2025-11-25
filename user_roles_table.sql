-- SQL to create user_roles table for Petcare project

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role VARCHAR(255) NOT NULL,
  PRIMARY KEY (user_id, role),
  CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id)
    REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
