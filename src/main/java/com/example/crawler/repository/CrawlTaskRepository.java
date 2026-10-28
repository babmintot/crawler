package com.example.crawler.repository;

import com.example.crawler.entity.CrawlTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlTaskRepository extends JpaRepository<CrawlTask, Long> {
}