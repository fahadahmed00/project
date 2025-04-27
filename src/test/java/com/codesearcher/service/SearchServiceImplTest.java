package com.codesearcher.service;

import com.codesearcher.dto.GitHubCodeItem;
import com.codesearcher.dto.SearchRequestDto;
import com.codesearcher.dto.SearchResponseDto;
import com.codesearcher.model.SearchHistory;
import com.codesearcher.repository.CodeSnippetRepository;
import com.codesearcher.repository.SearchHistoryRepository;
import com.codesearcher.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private GithubApiService githubApiService;

    @Mock
    private SearchHistoryRepository searchHistoryRepository;

    @Mock
    private CodeSnippetRepository codeSnippetRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    private SearchRequestDto searchRequestDto;
    private SearchHistory searchHistory;
    private List<GitHubCodeItem> githubResults;

    @BeforeEach
    void setUp() {
        searchRequestDto = SearchRequestDto.builder()
                .query("test query")
                .language("java")
                .build();

        searchHistory = SearchHistory.builder()
                .id(1L)
                .query("test query")
                .language("java")
                .timestamp(LocalDateTime.now())
                .totalResults(1)
                .sessionId("test-session")
                .build();

        GitHubCodeItem codeItem = GitHubCodeItem.builder()
                .name("Test.java")
                .path("src/Test.java")
                .repository("user/repo")
                .htmlUrl("https://github.com/user/repo/blob/main/src/Test.java")
                .content("public class Test {}")
                .language("Java")
                .size(100)
                .build();

        githubResults = List.of(codeItem);
    }

    @Test
    void searchCode_shouldReturnSearchResponseDto() {
        // Arrange
        when(githubApiService.searchCode(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(githubResults);
        when(searchHistoryRepository.save(any(SearchHistory.class)))
                .thenReturn(searchHistory);

        // Act
        SearchResponseDto result = searchService.searchCode(searchRequestDto, "test-session");

        // Assert
        assertNotNull(result);
        assertEquals(searchHistory.getId(), result.getSearchId());
        assertEquals(searchHistory.getQuery(), result.getQuery());
        assertEquals(searchHistory.getLanguage(), result.getLanguage());
        assertEquals(1, result.getSnippets().size());
        
        verify(githubApiService).searchCode(eq("test query"), eq("java"), isNull(), isNull());
        verify(searchHistoryRepository, times(2)).save(any(SearchHistory.class));
    }

    @Test
    void getSearchById_shouldReturnSearchResponseDto() {
        // Arrange
        when(searchHistoryRepository.findById(anyLong())).thenReturn(Optional.of(searchHistory));

        // Act
        SearchResponseDto result = searchService.getSearchById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(searchHistory.getId(), result.getSearchId());
        assertEquals(searchHistory.getQuery(), result.getQuery());
        
        verify(searchHistoryRepository).findById(1L);
    }

    @Test
    void getSearchHistory_withSessionId_shouldFilterBySession() {
        // Arrange
        when(searchHistoryRepository.findBySessionIdOrderByTimestampDesc(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(searchHistory)));

        // Act
        var result = searchService.getSearchHistory("test-session", Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(searchHistory.getId(), result.getContent().get(0).getId());
        
        verify(searchHistoryRepository).findBySessionIdOrderByTimestampDesc(eq("test-session"), any(Pageable.class));
        verify(searchHistoryRepository, never()).findAllByOrderByTimestampDesc(any(Pageable.class));
    }

    @Test
    void getSearchHistory_withoutSessionId_shouldReturnAllResults() {
        // Arrange
        when(searchHistoryRepository.findAllByOrderByTimestampDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(searchHistory)));

        // Act
        var result = searchService.getSearchHistory(null, Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        
        verify(searchHistoryRepository, never()).findBySessionIdOrderByTimestampDesc(anyString(), any(Pageable.class));
        verify(searchHistoryRepository).findAllByOrderByTimestampDesc(any(Pageable.class));
    }

    @Test
    void searchSnippets_shouldReturnMatchingSnippets() {
        // Arrange
        when(codeSnippetRepository.findByContentFullTextSearch(anyString(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        // Act
        var result = searchService.searchSnippets("test", Pageable.unpaged());

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        
        verify(codeSnippetRepository).findByContentFullTextSearch(eq("test"), any(Pageable.class));
    }
}