/*
  # Add Full-Text Search Capabilities
  
  1. Changes
    - Add FULLTEXT index to `code_snippet.content` column to enable efficient text search
    
  2. Purpose
    - Enables native MySQL full-text search for efficient code snippet content searching
    - Supports natural language mode queries with proper ranking of results
*/

-- Add FULLTEXT index to the content column for efficient text search
ALTER TABLE code_snippet 
ADD FULLTEXT INDEX ft_code_snippet_content (content);