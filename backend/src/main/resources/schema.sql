-- Drop constraints safely
DROP TABLE IF EXISTS registrations CASCADE;
DROP TABLE IF EXISTS student_in_group CASCADE;
DROP TABLE IF EXISTS events CASCADE;
DROP TABLE IF EXISTS event_group CASCADE;
DROP TABLE IF EXISTS grouptable CASCADE;
DROP TABLE IF EXISTS courses CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- USERS
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    age INT,
    email VARCHAR(255),
    password VARCHAR(255)
);

-- STUDENTS
CREATE TABLE students (
    student_number VARCHAR(255) PRIMARY KEY,
    lastname VARCHAR(255),
    name VARCHAR(255)
);

-- COURSES
CREATE TABLE courses (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    description VARCHAR(255)
);

-- GROUPTABLE (RENAMED FOR POSTGRES SAFETY)
CREATE TABLE grouptable (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100),
    course_id BIGINT NOT NULL REFERENCES courses(id)
);

-- EVENTS
CREATE TABLE events (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255),
    check_in_time TIMESTAMP,
    check_out_time TIMESTAMP,
    course_id BIGINT NOT NULL REFERENCES courses(id),
    rotation_code varchar(255)
);

-- REGISTRATIONS
CREATE TABLE registrations (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    date TIMESTAMP,
    type VARCHAR(50),
    event_id BIGINT NOT NULL REFERENCES events(id),
    student_id VARCHAR(255) NOT NULL REFERENCES students(student_number)
--     UNIQUE (type, student_id)
);

-- STUDENT_IN_GROUP
CREATE TABLE student_in_group (
    studentnumber VARCHAR(255) NOT NULL REFERENCES students(student_number),
    group_id BIGINT NOT NULL REFERENCES grouptable(id),
    PRIMARY KEY (studentnumber, group_id)
);

CREATE TABLE event_group (
    event_id BIGINT,
    group_id BIGINT,
    PRIMARY KEY (event_id, group_id),
    FOREIGN KEY (event_id) REFERENCES events,
    FOREIGN KEY (group_id) REFERENCES grouptable
);