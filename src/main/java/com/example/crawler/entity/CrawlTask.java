package com.example.crawler.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference; // <-- ВАЖНЫЙ ИМПОРТ
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "crawl_tasks")
public class CrawlTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String startUrl;

    @Enumerated(EnumType.STRING)
    private Status status;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference  // <-- Позволяет сериализовать список контактов, но не идти обратно в task
    private List<ContactData> contacts;

    public enum Status {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }
}