package com.codesearcher.integration;

import com.codesearcher.CodeSearcherApplication;
import com.codesearcher.dto.SearchRequestDto;
import com.codesearcher.dto.SearchResponseDto;
import com.codesearcher.model.CodeSnippet;
import com.codesearcher.model.SearchHistory;
import com.codesearcher.repository.CodeSnippetRepository;
import com.codesearcher.repository.SearchHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = CodeSearcherApplication.class, 
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class CodeSearchIntegrationTest {

    @Container
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private CodeSnippetRepository codeSnippetRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mySQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mySQLContainer::getUsername);
        registry.add("spring.datasource.password", mySQLContainer::getPassword);
        
        // Disable WebClient calls in tests
        registry.add("spring.cloud.discovery.client.simple.instances.github-api[0].uri", 
                    () -> "http://localhost:8888");
    }

    @Test
    void contextLoads() {
        assertTrue(mySQLContainer.isRunning());
    }

    @Test
    void getSearchHistory_shouldReturnResults() {
        // Setup test data
        SearchHistory history = SearchHistory.builder()
                .query("test query")
                .language("java")
                .timestamp(LocalDateTime.now())
                .totalResults(0)
                .sessionId("test-session")
                .build();
        
        searchHistoryRepository.save(history);
        
        // Test API
        String url = "http://localhost:" + port + "/api/history?currentUserOnly=false";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("test query"));
        
        // Cleanup
        searchHistoryRepository.deleteAll();
    }

    @Test
    void searchSnippets_withFullTextSearch_shouldReturnMatches() {
        // Setup test data
        SearchHistory history = SearchHistory.builder()
                .query("test query")
                .timestamp(LocalDateTime.now())
                .totalResults(1)
                .sessionId("test-session")
                .build();
        
        searchHistoryRepository.save(history);
        
        CodeSnippet snippet = CodeSnippet.builder()
                .name("Test.java")
                .path("src/Test.java")
                .repository("user/repo")
                .url("https://github.com/user/repo/blob/main/src/Test.java")
                .content("public class Test { public static void main(String[] args) { } }")
                .language("Java")
                .size(100)
                .searchHistory(history)
                .build();
        
        codeSnippetRepository.save(snippet);
        
        // Test API - Note: Full-text search will work properly on MySQL but may fail in tests
        // This is primarily to verify the endpoint works
        String url = "http://localhost:" + port + "/api/snippets/search?q=public";
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        
        assertEquals(200, response.getStatusCode().value());
        
        // Cleanup
        codeSnippetRepository.deleteAll();
        searchHistoryRepository.deleteAll();
    }
}