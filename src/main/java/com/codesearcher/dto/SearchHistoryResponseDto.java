package com.codesearcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryResponseDto {
    private Long id;
    private String query;
    private String language;
    private String repository;
    private String fileExtension;
    private LocalDateTime timestamp;
    private Integer totalResults;
}