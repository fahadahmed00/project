package com.codesearcher.controller;

import com.codesearcher.dto.*;
import com.codesearcher.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchControllerTest {

    @Mock
    private SearchService searchService;

    @InjectMocks
    private SearchController searchController;

    private MockHttpServletRequest request;
    private SearchRequestDto searchRequestDto;
    private SearchResponseDto searchResponseDto;
    private PageResponseDto<SearchHistoryResponseDto> historyResponseDto;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());

        searchRequestDto = SearchRequestDto.builder()
                .query("test")
                .language("java")
                .build();

        searchResponseDto = SearchResponseDto.builder()
                .searchId(1L)
                .query("test")
                .language("java")
                .timestamp(LocalDateTime.now())
                .totalResults(1)
                .snippets(List.of(new CodeSnippetDto()))
                .build();

        historyResponseDto = PageResponseDto.<SearchHistoryResponseDto>builder()
                .content(List.of(new SearchHistoryResponseDto()))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
    }

    @Test
    void searchCode_shouldReturnValidResponse() {
        // Arrange
        when(searchService.searchCode(any(SearchRequestDto.class), anyString()))
                .thenReturn(searchResponseDto);

        // Act
        ResponseEntity<SearchResponseDto> response = searchController.searchCode(searchRequestDto, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(searchResponseDto, response.getBody());
    }

    @Test
    void getSearchHistory_withCurrentUserOnly_shouldFilterBySession() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        Pageable pageable = PageRequest.of(0, 10, sort);
        
        when(searchService.getSearchHistory(anyString(), eq(pageable)))
                .thenReturn(historyResponseDto);

        // Act
        ResponseEntity<PageResponseDto<SearchHistoryResponseDto>> response = 
                searchController.getSearchHistory(0, 10, true, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(historyResponseDto, response.getBody());
    }

    @Test
    void getSearchHistory_withoutCurrentUserOnly_shouldReturnAllHistory() {
        // Arrange
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        Pageable pageable = PageRequest.of(0, 10, sort);
        
        when(searchService.getSearchHistory(isNull(), eq(pageable)))
                .thenReturn(historyResponseDto);

        // Act
        ResponseEntity<PageResponseDto<SearchHistoryResponseDto>> response = 
                searchController.getSearchHistory(0, 10, false, request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(historyResponseDto, response.getBody());
    }

    @Test
    void getSearchById_shouldReturnSearchDetails() {
        // Arrange
        when(searchService.getSearchById(anyLong()))
                .thenReturn(searchResponseDto);

        // Act
        ResponseEntity<SearchResponseDto> response = searchController.getSearchById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(searchResponseDto, response.getBody());
    }

    @Test
    void searchSnippets_shouldReturnMatchingSnippets() {
        // Arrange
        PageResponseDto<CodeSnippetDto> snippetsResponse = PageResponseDto.<CodeSnippetDto>builder()
                .content(List.of(new CodeSnippetDto()))
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .last(true)
                .build();
        
        when(searchService.searchSnippets(anyString(), any(Pageable.class)))
                .thenReturn(snippetsResponse);

        // Act
        ResponseEntity<PageResponseDto<CodeSnippetDto>> response = 
                searchController.searchSnippets("test", 0, 10);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(snippetsResponse, response.getBody());
    }
}