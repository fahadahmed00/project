package com.codesearcher.controller;

import com.codesearcher.dto.*;
import com.codesearcher.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Search", description = "GitHub Code Search APIs")
public class SearchController {

    private final SearchService searchService;

    @PostMapping("/search")
    @Operation(
        summary = "Search for code on GitHub",
        description = "Search for code snippets on GitHub based on query parameters and filters",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", 
                         content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "500", description = "Error occurred during search",
                         content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    public ResponseEntity<SearchResponseDto> searchCode(
            @Valid @RequestBody SearchRequestDto searchRequestDto,
            HttpServletRequest request) {
        
        // Get or create session ID for tracking anonymous user
        String sessionId = getOrCreateSessionId(request);
        
        SearchResponseDto result = searchService.searchCode(searchRequestDto, sessionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    @Operation(
        summary = "Get search history",
        description = "Retrieve paginated search history, optionally filtered by the current user's session",
        responses = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully")
        }
    )
    public ResponseEntity<PageResponseDto<SearchHistoryResponseDto>> getSearchHistory(
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Whether to include only the current user's history")
            @RequestParam(defaultValue = "true") boolean currentUserOnly,
            
            HttpServletRequest request) {
        
        String sessionId = currentUserOnly ? getOrCreateSessionId(request) : null;
        
        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        PageResponseDto<SearchHistoryResponseDto> history = 
                searchService.getSearchHistory(sessionId, pageable);
        
        return ResponseEntity.ok(history);
    }

    @GetMapping("/search/{id}")
    @Operation(
        summary = "Get search details by ID",
        description = "Retrieve detailed information about a specific search including all code snippets",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Search not found", 
                         content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
        }
    )
    public ResponseEntity<SearchResponseDto> getSearchById(
            @Parameter(description = "ID of the search to retrieve")
            @PathVariable Long id) {
        
        SearchResponseDto searchDetails = searchService.getSearchById(id);
        return ResponseEntity.ok(searchDetails);
    }

    @GetMapping("/snippets/search")
    @Operation(
        summary = "Full-text search in code snippets",
        description = "Search within the content of cached code snippets using MySQL full-text search",
        responses = {
            @ApiResponse(responseCode = "200", description = "Search completed successfully")
        }
    )
    public ResponseEntity<PageResponseDto<CodeSnippetDto>> searchSnippets(
            @Parameter(description = "Search query text")
            @RequestParam String q,
            
            @Parameter(description = "Page number (zero-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of items per page")
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        PageResponseDto<CodeSnippetDto> results = searchService.searchSnippets(q, pageable);
        
        return ResponseEntity.ok(results);
    }

    private String getOrCreateSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String sessionId = (String) session.getAttribute("ANONYMOUS_ID");
        
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
            session.setAttribute("ANONYMOUS_ID", sessionId);
        }
        
        return sessionId;
    }
}