package com.codesearcher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "search_history")
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String query;

    private String language;

    private String repository;

    @Column(name = "file_extension")
    private String fileExtension;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "total_results")
    private Integer totalResults;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @OneToMany(mappedBy = "searchHistory", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CodeSnippet> snippets = new ArrayList<>();
}