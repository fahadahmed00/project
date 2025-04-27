package com.codesearcher.service.impl;

import com.codesearcher.dto.GitHubCodeItem;
import com.codesearcher.dto.GitHubSearchResponse;
import com.codesearcher.exception.GithubApiException;
import com.codesearcher.service.GithubApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GithubApiServiceImpl implements GithubApiService {

    private final WebClient githubWebClient;
    private final ReactiveCircuitBreakerFactory circuitBreakerFactory;

    @Value("${github.api.search-code-path}")
    private String searchCodePath;

    @Value("${github.api.items-per-page}")
    private int itemsPerPage;

    @Override
    @Cacheable(value = "githubSearchResults", key = "#query + '_' + #language + '_' + #repository + '_' + #fileExtension")
    public List<GitHubCodeItem> searchCode(String query, String language, String repository, String fileExtension) {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("githubApi");

        // Build the query string with filters
        StringBuilder queryBuilder = new StringBuilder(query);
        
        if (language != null && !language.isEmpty()) {
            queryBuilder.append(" language:").append(language);
        }
        
        if (repository != null && !repository.isEmpty()) {
            queryBuilder.append(" repo:").append(repository);
        }
        
        if (fileExtension != null && !fileExtension.isEmpty()) {
            queryBuilder.append(" extension:").append(fileExtension.replace(".", ""));
        }

        String uri = UriComponentsBuilder.fromPath(searchCodePath)
                .queryParam("q", queryBuilder.toString())
                .queryParam("per_page", itemsPerPage)
                .build()
                .toUriString();

        log.info("Searching GitHub code API with URI: {}", uri);

        try {
            GitHubSearchResponse response = circuitBreaker.run(
                githubWebClient.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(t -> HttpStatus.isError(t), clientResponse -> {
                        log.error("GitHub API error: {}", clientResponse.statusCode());
                        return Mono.error(new GithubApiException("GitHub API returned error: " + clientResponse.statusCode()));
                    })
                    .bodyToMono(GitHubSearchResponse.class)
                    .timeout(Duration.ofSeconds(5)),
                throwable -> {
                    log.error("Circuit breaker fallback triggered: {}", throwable.getMessage());
                    return Mono.just(GitHubSearchResponse.builder()
                            .totalCount(0)
                            .incompleteResults(true)
                            .items(Collections.emptyList())
                            .build());
                }
            ).block();

            if (response == null || response.getItems() == null) {
                return Collections.emptyList();
            }

            // For each item, fetch the file content
            return response.getItems().stream()
                    .map(item -> {
                        String[] repoParts = item.getHtmlUrl().split("/blob/")[0].split("/");
                        String owner = repoParts[repoParts.length - 2];
                        String repo = repoParts[repoParts.length - 1];
                        String path = item.getPath();
                        
                        String content = getFileContent(owner, repo, path);
                        item.setContent(content);
                        item.setRepository(owner + "/" + repo);
                        
                        return item;
                    })
                    .filter(item -> item.getContent() != null && !item.getContent().isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error searching GitHub code: {}", e.getMessage());
            throw new GithubApiException("Failed to search GitHub code: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "githubFileContents", key = "#owner + '_' + #repo + '_' + #path")
    public String getFileContent(String owner, String repo, String path) {
        ReactiveCircuitBreaker circuitBreaker = circuitBreakerFactory.create("githubApi");

        try {
            String uri = "/repos/" + owner + "/" + repo + "/contents/" + path;
            
            log.debug("Fetching file content from GitHub: {}", uri);
            
            GitHubContentResponse response = circuitBreaker.run(
                githubWebClient.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(t -> {
                        boolean error = HttpStatus.isError(t);
                        return error;
                    }, clientResponse -> {
                        log.error("GitHub API error fetching content: {}", clientResponse.statusCode());
                        return Mono.error(new GithubApiException("GitHub API returned error: " + clientResponse.statusCode()));
                    })
                    .bodyToMono(GitHubContentResponse.class)
                    .timeout(Duration.ofSeconds(5)),
                throwable -> {
                    log.error("Circuit breaker fallback triggered for content fetch: {}", throwable.getMessage());
                    return Mono.empty();
                }
            ).block();

            if (response == null || response.getContent() == null) {
                return "";
            }

            // Decode base64 content
            return new String(Base64.getDecoder().decode(response.getContent().replaceAll("\\s", "")));
        } catch (Exception e) {
            log.error("Error fetching file content: {}", e.getMessage());
            return ""; // Return empty content on error
        }
    }

    // Inner class for GitHub content API response
    @lombok.Data
    private static class GitHubContentResponse {
        private String name;
        private String path;
        private String content;
        private String encoding;
    }
}