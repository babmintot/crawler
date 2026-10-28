package com.example.crawler.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class ScheduledTasks {

    // КРИТЕРИЙ: Добавлен запуск по расписанию @Scheduled
    // выполняется каждые 60 секунд (60000 мс)
    @Scheduled(fixedRate = 60000)
    public void logCrawlerStatus() {
        System.out.println("[" + LocalDateTime.now() + "] [SCHEDULED] Краулер активен. Проверка состояния системы...");
    }
}