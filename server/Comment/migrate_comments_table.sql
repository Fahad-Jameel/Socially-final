-- Migration script to update comments table structure
-- Run this SQL to add necessary columns to the comments table

ALTER TABLE comments 
ADD COLUMN comment_id INT AUTO_INCREMENT PRIMARY KEY FIRST,
ADD COLUMN user_id INT AFTER comment_id,
ADD COLUMN timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP AFTER comment_text;

-- Drop old foreign key if exists
ALTER TABLE comments DROP FOREIGN KEY comments_ibfk_1;

-- Add proper foreign keys
ALTER TABLE comments 
ADD FOREIGN KEY (post_id) REFERENCES posts(post_id) ON DELETE CASCADE,
ADD FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

