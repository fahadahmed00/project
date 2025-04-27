package com.codesearcher.service;

import com.codesearcher.dto.GitHubCodeItem;
import java.util.List;

public interface GithubApiService {

    /**
     * Search for code on GitHub using the provided parameters
     * 
     * @param query The search query text
     * @param language Optional language filter
     * @param repository Optional repository filter (format: owner/repo)
     * @param fileExtension Optional file extension filter
     * @return List of code items matching the search criteria
     */
    List<GitHubCodeItem> searchCode(String query, String language, String repository, String fileExtension);
    
    /**
     * Get content of a specific file from GitHub
     * 
     * @param owner Repository owner
     * @param repo Repository name
     * @param path File path
     * @return The file content
     */
    String getFileContent(String owner, String repo, String path);
}