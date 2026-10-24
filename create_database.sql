-- Run this once in MySQL Workbench (or the mysql CLI).
CREATE DATABASE IF NOT EXISTS student_management
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE student_management;

-- OPTIONAL: Hibernate creates these tables automatically (ddl-auto=update).
-- They are listed here so you can see/understand the schema.
CREATE TABLE IF NOT EXISTS users (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  name       VARCHAR(100) NOT NULL,
  email      VARCHAR(150) NOT NULL,
  password   VARCHAR(255) NOT NULL,
  role       VARCHAR(20)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_email (email)
);

CREATE TABLE IF NOT EXISTS students (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  name       VARCHAR(100) NOT NULL,
  email      VARCHAR(150) NOT NULL,
  branch     VARCHAR(100) NOT NULL,
  year       INT          NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_students_email (email)
);
