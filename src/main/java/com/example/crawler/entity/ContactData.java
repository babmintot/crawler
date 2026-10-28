package com.example.crawler.entity;

import com.fasterxml.jackson.annotation.JsonBackReference; // <-- ВАЖНЫЙ ИМПОРТ
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "contact_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContactData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceUrl;

    @Column(nullable = false)
    private String contactType; // EMAIL, PHONE, ADDRESS

    @Column(nullable = false, length = 1000)
    private String contactValue;

    @Column(nullable = false)
    private LocalDateTime foundAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    @JsonBackReference  // <-- Предотвращает бесконечную рекурсию при сериализации в JSON
    private CrawlTask task;
}