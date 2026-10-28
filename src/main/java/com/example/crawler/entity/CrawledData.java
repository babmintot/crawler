package com.example.crawler.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "crawled_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawledData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceUrl; // URL страницы, где нашли данные

    @Column(nullable = false)
    private String contactType; // Тип: "EMAIL", "PHONE" или "ADDRESS"

    @Column(nullable = false, length = 1000)
    private String contactValue; // Само значение (email, телефон или адрес)

    @Column(nullable = false)
    private LocalDateTime foundAt; // Время обнаружения
}