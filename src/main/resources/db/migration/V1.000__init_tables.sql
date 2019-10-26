CREATE TABLE users(
    id int primary key,
    is_admin boolean not null,
    name varchar(255)
);

CREATE TABLE events(
    id SERIAL primary key,
    name varchar(1024) not null
);

CREATE TABLE feedbacks(
    id SERIAL primary key,
    text varchar(1024) not null,
    satisfaction_level int not null,
    user_id int,
    event_id serial,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
)