-- Жанры
CREATE TABLE IF NOT EXISTS genres
(
    genre_id INT PRIMARY KEY,
    name     VARCHAR(30) NOT NULL
);

-- Рейтинги MPA
CREATE TABLE IF NOT EXISTS mpa_ratings
(
    mpa_id TINYINT PRIMARY KEY,
    name   VARCHAR(5) NOT NULL
);

-- Пользователи
CREATE TABLE IF NOT EXISTS users
(
    user_id  INT PRIMARY KEY AUTO_INCREMENT,
    email    VARCHAR(255) NOT NULL UNIQUE,
    login    VARCHAR(30)  NOT NULL UNIQUE,
    name     VARCHAR(255),
    birthday DATE
);

-- Фильмы
CREATE TABLE IF NOT EXISTS films
(
    film_id      INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(200) NOT NULL,
    description  TEXT,
    release_date DATE         NOT NULL,
    duration     INT          NOT NULL CHECK (duration > 0),
    mpa_id       TINYINT,
    FOREIGN KEY (mpa_id) REFERENCES mpa_ratings (mpa_id)
);

-- Жанры фильмов
CREATE TABLE IF NOT EXISTS film_genres
(
    film_id  INT,
    genre_id SMALLINT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE,
    FOREIGN KEY (genre_id) REFERENCES genres (genre_id) ON DELETE CASCADE
);

-- Друзья пользователей
CREATE TABLE IF NOT EXISTS friendships
(
    user_id      INT,
    friend_id    INT,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES users (user_id) ON DELETE CASCADE,
    is_confirmed BOOLEAN NOT NULL
);

-- Лайки фильмов
CREATE TABLE IF NOT EXISTS film_likes
(
    film_id INT,
    user_id INT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);
