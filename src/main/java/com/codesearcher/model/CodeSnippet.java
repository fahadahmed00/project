package com.codesearcher.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "code_snippet", indexes = {
    @Index(name = "idx_content_ft", columnList = "content")
})
public class CodeSnippet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String path;

    @Column(nullable = false)
    private String repository;

    @Column(nullable = false)
    private String url;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    private String language;

    private Integer size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_history_id", nullable = false)
    private SearchHistory searchHistory;
}