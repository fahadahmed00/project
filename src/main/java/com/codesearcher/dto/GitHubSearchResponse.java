package com.codesearcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubSearchResponse {
    @JsonProperty("total_count")
    private Integer totalCount;
    @JsonProperty("incomplete_results")
    private Boolean incompleteResults;
    private List<GitHubCodeItem> items;
}