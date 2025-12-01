-- Migration script to add video quality support to movies
-- This script is for reference only, JPA will auto-create tables

-- Create video_qualities table
CREATE TABLE IF NOT EXISTS video_qualities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    quality VARCHAR(50) NOT NULL,
    video_url TEXT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE,
    INDEX idx_movie_id (movie_id)
);

-- Remove video_url column from movies table (if exists)
-- Note: This will drop the column and its data!
-- Backup your data before running this
ALTER TABLE movies DROP COLUMN IF EXISTS video_url;

-- Example data: Insert sample video qualities for existing movies
-- INSERT INTO video_qualities (movie_id, quality, video_url, is_default)
-- VALUES
--   (1, '360p', 'http://example.com/movie1_360p.m3u8', FALSE),
--   (1, '720p', 'http://example.com/movie1_720p.m3u8', TRUE),
--   (1, '1080p', 'http://example.com/movie1_1080p.m3u8', FALSE);

