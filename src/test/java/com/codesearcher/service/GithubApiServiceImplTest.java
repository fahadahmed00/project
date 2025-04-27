package com.codesearcher.service;

import com.codesearcher.dto.GitHubCodeItem;
import com.codesearcher.exception.GithubApiException;
import com.codesearcher.service.impl.GithubApiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubApiServiceImplTest {

    @Mock
    private WebClient githubWebClient;

    @Mock
    private ReactiveCircuitBreakerFactory circuitBreakerFactory;

    @Mock
    private ReactiveCircuitBreaker circuitBreaker;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private GithubApiServiceImpl githubApiService;

    @BeforeEach
    void setUp() {
        // Set required properties via reflection
        ReflectionTestUtils.setField(githubApiService, "searchCodePath", "/search/code");
        ReflectionTestUtils.setField(githubApiService, "itemsPerPage", 30);

        // Mock WebClient chain
        when(githubWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        // Mock circuit breaker
        when(circuitBreakerFactory.create(anyString())).thenReturn(circuitBreaker);
    }

    @Test
    void searchCode_shouldReturnValidResults() {
        // Arrange
        // Mock response with GitHubSearchResponse containing items
        com.codesearcher.dto.GitHubSearchResponse mockResponse = mock(com.codesearcher.dto.GitHubSearchResponse.class);
        GitHubCodeItem mockItem = GitHubCodeItem.builder()
                .name("Test.java")
                .path("src/Test.java")
                .htmlUrl("https://github.com/user/repo/blob/main/src/Test.java")
                .build();
        
        when(mockResponse.getItems()).thenReturn(List.of(mockItem));
        when(responseSpec.bodyToMono(com.codesearcher.dto.GitHubSearchResponse.class))
                .thenReturn(Mono.just(mockResponse));
        when(circuitBreaker.run(any(Mono.class), any())).thenAnswer(invocation -> {
            Mono<?> mono = invocation.getArgument(0);
            return mono;
        });

        // Mock file content retrieval
        doReturn("public class Test {}").when(githubApiService).getFileContent(anyString(), anyString(), anyString());

        // Act
        List<GitHubCodeItem> results = githubApiService.searchCode("test", "java", null, null);

        // Assert
        assertNotNull(results);
        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Test.java", results.get(0).getName());

        // Verify interactions
        verify(githubWebClient).get();
        verify(requestHeadersUriSpec).uri(anyString());
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(com.codesearcher.dto.GitHubSearchResponse.class);
        verify(circuitBreaker).run(any(Mono.class), any());
    }

    @Test
    void searchCode_whenApiError_shouldHandleGracefully() {
        // Arrange
        when(responseSpec.bodyToMono(com.codesearcher.dto.GitHubSearchResponse.class))
                .thenReturn(Mono.error(new RuntimeException("API Error")));
        when(circuitBreaker.run(any(Mono.class), any())).thenAnswer(invocation -> {
            Mono<?> mono = invocation.getArgument(0);
            return mono;
        });

        // Act & Assert
        assertThrows(GithubApiException.class, () -> 
            githubApiService.searchCode("test", "java", null, null)
        );
    }
}