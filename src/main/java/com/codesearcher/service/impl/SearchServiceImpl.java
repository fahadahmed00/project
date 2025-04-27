package com.codesearcher.service.impl;

import com.codesearcher.dto.*;
import com.codesearcher.exception.GithubApiException;
import com.codesearcher.exception.ResourceNotFoundException;
import com.codesearcher.model.CodeSnippet;
import com.codesearcher.model.SearchHistory;
import com.codesearcher.repository.CodeSnippetRepository;
import com.codesearcher.repository.SearchHistoryRepository;
import com.codesearcher.service.GithubApiService;
import com.codesearcher.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

    private final GithubApiService githubApiService;
    private final SearchHistoryRepository searchHistoryRepository;
    private final CodeSnippetRepository codeSnippetRepository;

    @Override
    @Transactional
    public SearchResponseDto searchCode(SearchRequestDto searchRequestDto, String sessionId) {
        log.info("Performing search for query: {}, language: {}, repository: {}, fileExtension: {}",
                searchRequestDto.getQuery(), searchRequestDto.getLanguage(), 
                searchRequestDto.getRepository(), searchRequestDto.getFileExtension());

        // Call GitHub API
        List<GitHubCodeItem> githubResults = githubApiService.searchCode(
                searchRequestDto.getQuery(),
                searchRequestDto.getLanguage(),
                searchRequestDto.getRepository(),
                searchRequestDto.getFileExtension()
        );

        // Save search history
        SearchHistory searchHistory = SearchHistory.builder()
                .query(searchRequestDto.getQuery())
                .language(searchRequestDto.getLanguage())
                .repository(searchRequestDto.getRepository())
                .fileExtension(searchRequestDto.getFileExtension())
                .timestamp(LocalDateTime.now())
                .totalResults(githubResults.size())
                .sessionId(sessionId)
                .build();
        
        searchHistoryRepository.save(searchHistory);

        // Create and save code snippets
        List<CodeSnippet> snippets = githubResults.stream()
                .map(item -> CodeSnippet.builder()
                        .name(item.getName())
                        .path(item.getPath())
                        .repository(item.getRepository())
                        .url(item.getHtmlUrl())
                        .content(item.getContent())
                        .language(item.getLanguage())
                        .size(item.getSize())
                        .searchHistory(searchHistory)
                        .build())
                .collect(Collectors.toList());
        
        searchHistory.setSnippets(snippets);
        searchHistoryRepository.save(searchHistory);

        // Map to response DTO
        List<CodeSnippetDto> snippetDtos = snippets.stream()
                .map(this::mapToCodeSnippetDto)
                .collect(Collectors.toList());

        return SearchResponseDto.builder()
                .searchId(searchHistory.getId())
                .query(searchHistory.getQuery())
                .language(searchHistory.getLanguage())
                .repository(searchHistory.getRepository())
                .fileExtension(searchHistory.getFileExtension())
                .timestamp(searchHistory.getTimestamp())
                .totalResults(searchHistory.getTotalResults())
                .snippets(snippetDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponseDto<SearchHistoryResponseDto> getSearchHistory(String sessionId, Pageable pageable) {
        Page<SearchHistory> historyPage;
        
        if (sessionId != null && !sessionId.isEmpty()) {
            historyPage = searchHistoryRepository.findBySessionIdOrderByTimestampDesc(sessionId, pageable);
        } else {
            historyPage = searchHistoryRepository.findAllByOrderByTimestampDesc(pageable);
        }

        List<SearchHistoryResponseDto> historyDtos = historyPage.getContent().stream()
                .map(this::mapToSearchHistoryDto)
                .collect(Collectors.toList());

        return PageResponseDto.<SearchHistoryResponseDto>builder()
                .content(historyDtos)
                .pageNumber(historyPage.getNumber())
                .pageSize(historyPage.getSize())
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .last(historyPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SearchResponseDto getSearchById(Long searchId) {
        SearchHistory searchHistory = searchHistoryRepository.findById(searchId)
                .orElseThrow(() -> new ResourceNotFoundException("Search history not found with id: " + searchId));

        List<CodeSnippetDto> snippetDtos = searchHistory.getSnippets().stream()
                .map(this::mapToCodeSnippetDto)
                .collect(Collectors.toList());

        return SearchResponseDto.builder()
                .searchId(searchHistory.getId())
                .query(searchHistory.getQuery())
                .language(searchHistory.getLanguage())
                .repository(searchHistory.getRepository())
                .fileExtension(searchHistory.getFileExtension())
                .timestamp(searchHistory.getTimestamp())
                .totalResults(searchHistory.getTotalResults())
                .snippets(snippetDtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "snippetSearchResults", key = "#query + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public PageResponseDto<CodeSnippetDto> searchSnippets(String query, Pageable pageable) {
        Page<CodeSnippet> snippetsPage = codeSnippetRepository.findByContentFullTextSearch(query, pageable);

        List<CodeSnippetDto> snippetDtos = snippetsPage.getContent().stream()
                .map(this::mapToCodeSnippetDto)
                .collect(Collectors.toList());

        return PageResponseDto.<CodeSnippetDto>builder()
                .content(snippetDtos)
                .pageNumber(snippetsPage.getNumber())
                .pageSize(snippetsPage.getSize())
                .totalElements(snippetsPage.getTotalElements())
                .totalPages(snippetsPage.getTotalPages())
                .last(snippetsPage.isLast())
                .build();
    }

    private SearchHistoryResponseDto mapToSearchHistoryDto(SearchHistory searchHistory) {
        return SearchHistoryResponseDto.builder()
                .id(searchHistory.getId())
                .query(searchHistory.getQuery())
                .language(searchHistory.getLanguage())
                .repository(searchHistory.getRepository())
                .fileExtension(searchHistory.getFileExtension())
                .timestamp(searchHistory.getTimestamp())
                .totalResults(searchHistory.getTotalResults())
                .build();
    }

    private CodeSnippetDto mapToCodeSnippetDto(CodeSnippet codeSnippet) {
        return CodeSnippetDto.builder()
                .id(codeSnippet.getId())
                .name(codeSnippet.getName())
                .path(codeSnippet.getPath())
                .repository(codeSnippet.getRepository())
                .url(codeSnippet.getUrl())
                .content(codeSnippet.getContent())
                .language(codeSnippet.getLanguage())
                .size(codeSnippet.getSize())
                .build();
    }
}