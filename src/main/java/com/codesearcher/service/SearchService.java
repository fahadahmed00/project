package com.codesearcher.service;

import com.codesearcher.dto.CodeSnippetDto;
import com.codesearcher.dto.SearchRequestDto;
import com.codesearcher.dto.SearchResponseDto;
import com.codesearcher.dto.PageResponseDto;
import com.codesearcher.dto.SearchHistoryResponseDto;
import com.codesearcher.exception.GithubApiException;
import com.codesearcher.exception.ResourceNotFoundException;
import com.codesearcher.model.CodeSnippet;
import com.codesearcher.model.SearchHistory;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SearchService {

    /**
     * Perform a search on GitHub's code search API
     * @param searchRequestDto the search request containing query and filters
     * @param sessionId the anonymous session ID for tracking
     * @return a response with search results and metadata
     * @throws GithubApiException if the GitHub API call fails
     */
    SearchResponseDto searchCode(SearchRequestDto searchRequestDto, String sessionId);

    /**
     * Get paginated search history
     * @param sessionId optional session ID to filter by
     * @param pageable pagination parameters
     * @return a page of search history entries
     */
    PageResponseDto<SearchHistoryResponseDto> getSearchHistory(String sessionId, Pageable pageable);

    /**
     * Get detailed search results by ID
     * @param searchId the ID of the search to retrieve
     * @return the search response with snippets
     * @throws ResourceNotFoundException if the search is not found
     */
    SearchResponseDto getSearchById(Long searchId);

    /**
     * Perform a full-text search on cached code snippets
     * @param query the search text
     * @param pageable pagination parameters
     * @return a page of matching code snippets
     */
    PageResponseDto<CodeSnippetDto> searchSnippets(String query, Pageable pageable);
}