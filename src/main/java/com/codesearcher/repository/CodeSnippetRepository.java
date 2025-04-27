package com.codesearcher.repository;

import com.codesearcher.model.CodeSnippet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CodeSnippetRepository extends JpaRepository<CodeSnippet, Long> {

    @Query(value = "SELECT * FROM code_snippet WHERE MATCH(content) AGAINST (:searchText IN NATURAL LANGUAGE MODE)",
           nativeQuery = true)
    Page<CodeSnippet> findByContentFullTextSearch(@Param("searchText") String searchText, Pageable pageable);

    Page<CodeSnippet> findByLanguageIgnoreCase(String language, Pageable pageable);

    Page<CodeSnippet> findByRepositoryContainingIgnoreCase(String repository, Pageable pageable);

    Page<CodeSnippet> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT c FROM CodeSnippet c WHERE c.searchHistory.id = :searchHistoryId")
    Page<CodeSnippet> findBySearchHistoryId(@Param("searchHistoryId") Long searchHistoryId, Pageable pageable);
}