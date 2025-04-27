/*
  # Initial Schema Creation
  
  1. New Tables
    - `search_history`: Stores search metadata
      - `id` (bigint, primary key)
      - `query` (varchar, required)
      - `language` (varchar, optional)
      - `repository` (varchar, optional)
      - `file_extension` (varchar, optional)
      - `timestamp` (datetime, required)
      - `total_results` (int, required)
      - `session_id` (varchar, required)
    
    - `code_snippet`: Stores code snippets from search results
      - `id` (bigint, primary key)
      - `name` (varchar, required)
      - `path` (varchar, required)
      - `repository` (varchar, required)
      - `url` (varchar, required)
      - `content` (text, required)
      - `language` (varchar, optional)
      - `size` (int, optional)
      - `search_history_id` (bigint, foreign key)
  
  2. Indexes
    - Foreign key index on `code_snippet.search_history_id`
    - Session ID index on `search_history.session_id`
    - Timestamp index on `search_history.timestamp`
*/

-- Create the search_history table to track search metadata
CREATE TABLE IF NOT EXISTS search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    query VARCHAR(255) NOT NULL,
    language VARCHAR(50),
    repository VARCHAR(100),
    file_extension VARCHAR(20),
    timestamp DATETIME NOT NULL,
    total_results INT NOT NULL,
    session_id VARCHAR(36) NOT NULL
);

-- Create the code_snippet table to store search results
CREATE TABLE IF NOT EXISTS code_snippet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    path VARCHAR(255) NOT NULL,
    repository VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    language VARCHAR(50),
    size INT,
    search_history_id BIGINT NOT NULL,
    CONSTRAINT fk_search_history FOREIGN KEY (search_history_id) REFERENCES search_history(id)
);

-- Create indexes for optimizing queries
CREATE INDEX idx_search_history_session_id ON search_history(session_id);
CREATE INDEX idx_search_history_timestamp ON search_history(timestamp);
CREATE INDEX idx_code_snippet_search_history_id ON code_snippet(search_history_id);