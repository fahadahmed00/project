package com.codesearcher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSnippetDto {
    private Long id;
    private String name;
    private String path;
    private String repository;
    private String url;
    private String content;
    private String language;
    private Integer size;
}