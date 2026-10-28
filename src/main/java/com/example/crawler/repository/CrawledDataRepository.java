package com.example.crawler.repository;

import com.example.crawler.entity.CrawledData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawledDataRepository extends JpaRepository<CrawledData, Long> {

    // Поиск по типу контакта (например, только EMAIL) с пагинацией и сортировкой
    Page<CrawledData> findByContactType(String contactType, Pageable pageable);

    // Поиск по частичному совпадению значения (например, домен "@netology.ru")
    Page<CrawledData> findByContactValueContainingIgnoreCase(String keyword, Pageable pageable);
}