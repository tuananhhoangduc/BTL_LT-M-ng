CREATE DATABASE IF NOT EXISTS color_mind_game
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE color_mind_game;

CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    total_points DECIMAL(10,1) NOT NULL DEFAULT 0.0,
    total_wins INT UNSIGNED NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_users_points CHECK (total_points >= 0)
) ENGINE = InnoDB;

CREATE TABLE matches (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    end_reason ENUM(
        'TURNS_FINISHED',
        'BOARD_FULL',
        'SURRENDER',
        'LEAVE_GAME',
        'DISCONNECTED'
    ) NOT NULL,
    started_at DATETIME NOT NULL,
    ended_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_matches_ended_at (ended_at)
) ENGINE = InnoDB;

CREATE TABLE match_players (
    match_id BIGINT UNSIGNED NOT NULL,
    user_id BIGINT UNSIGNED NOT NULL,
    color ENUM('RED', 'BLUE') NOT NULL,
    captured_cells SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    result ENUM('WIN', 'DRAW', 'LOSS') NOT NULL,
    points_earned DECIMAL(3,1) NOT NULL DEFAULT 0.0,
    PRIMARY KEY (match_id, user_id),
    UNIQUE KEY uq_match_color (match_id, color),
    CONSTRAINT fk_match_players_match
        FOREIGN KEY (match_id) REFERENCES matches(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_match_players_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_match_points CHECK (points_earned >= 0)
) ENGINE = InnoDB;

CREATE INDEX idx_match_players_user
    ON match_players(user_id, match_id);

CREATE INDEX idx_users_points
    ON users(total_points DESC, total_wins DESC, username ASC);

CREATE INDEX idx_users_wins
    ON users(total_wins DESC, total_points DESC, username ASC);

-- Ranking theo tong diem
SELECT username, total_points, total_wins
FROM users
ORDER BY total_points DESC, total_wins DESC, username ASC;

-- Ranking theo tong so tran thang
SELECT username, total_wins, total_points
FROM users
ORDER BY total_wins DESC, total_points DESC, username ASC;

-- Lich su dau cua mot nguoi choi
SELECT
    m.id AS match_id,
    m.started_at,
    m.ended_at,
    m.end_reason,
    opponent.username AS opponent,
    mp.color,
    mp.captured_cells,
    mp.result,
    mp.points_earned
FROM match_players mp
JOIN matches m ON m.id = mp.match_id
JOIN match_players opponent_player
    ON opponent_player.match_id = mp.match_id
   AND opponent_player.user_id <> mp.user_id
JOIN users opponent ON opponent.id = opponent_player.user_id
WHERE mp.user_id = ?
ORDER BY m.ended_at DESC;
