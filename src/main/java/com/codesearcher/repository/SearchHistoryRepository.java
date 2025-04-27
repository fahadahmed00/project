package com.codesearcher.repository;

import com.codesearcher.model.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    
    Page<SearchHistory> findAllByOrderByTimestampDesc(Pageable pageable);
    
    Page<SearchHistory> findBySessionIdOrderByTimestampDesc(String sessionId, Pageable pageable);
    
    Page<SearchHistory> findByQueryContainingIgnoreCaseOrderByTimestampDesc(String query, Pageable pageable);
    
    Page<SearchHistory> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end, Pageable pageable);
}