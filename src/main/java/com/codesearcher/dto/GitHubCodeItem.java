package com.codesearcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubCodeItem {
    private String name;
    private String path;
    private String repository;
    @JsonProperty("html_url")
    private String htmlUrl;
    private String content;
    private String language;
    private Integer size;
}