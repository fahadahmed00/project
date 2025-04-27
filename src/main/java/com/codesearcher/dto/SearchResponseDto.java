package com.codesearcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponseDto {
    private Long searchId;
    private String query;
    private String language;
    private String repository;
    private String fileExtension;
    private LocalDateTime timestamp;
    private Integer totalResults;
    private List<CodeSnippetDto> snippets;
}