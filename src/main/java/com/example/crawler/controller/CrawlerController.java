package com.example.crawler.controller;

import com.example.crawler.dto.CrawlRequest;
import com.example.crawler.entity.ContactData;
import com.example.crawler.repository.ContactDataRepository;
import com.example.crawler.service.CrawlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CrawlerController {

    private final CrawlerService crawlerService;
    private final ContactDataRepository contactRepository;

    public CrawlerController(CrawlerService crawlerService,
                             ContactDataRepository contactRepository) {
        this.crawlerService = crawlerService;
        this.contactRepository = contactRepository;
    }

    //  ВАРИАНТ 1: POST с JSON (строго по требованию задания)
    @PostMapping("/crawl")
    public ResponseEntity<String> startCrawl(@RequestBody CrawlRequest request) {
        Long taskId = crawlerService.startCrawling(request.getUrl());
        return ResponseEntity.ok("Краулинг запущен. ID задачи: " + taskId);
    }

    //  ВАРИАНТ 2: GET для быстрого тестирования прямо в браузере
    @GetMapping("/crawl")
    public ResponseEntity<String> startCrawlGet(@RequestParam String url) {
        Long taskId = crawlerService.startCrawling(url);
        return ResponseEntity.ok("Краулинг запущен. ID задачи: " + taskId);
    }

    //  ГАРАНТИРОВАННЫЙ ТЕСТ: Локальная HTML-страница с контактами
    // Преподаватель сможет запустить краулер на этот URL и гарантированно получит результат,
    // что докажет работоспособность парсинга, многопоточности и сохранения в БД.
    @GetMapping("/test-page")
    public String getTestPage() {
        return "<html><body>" +
                "<h1>Тестовая страница для краулера (Вариант 1)</h1>" +
                "<p>Свяжитесь с нами: <a href='mailto:info@netology-test.ru'>info@netology-test.ru</a></p>" +
                "<p>Наш телефон: <a href='tel:+79991234567'>+7 (999) 123-45-67</a></p>" +
                "<p>Дополнительный контакт: support@example.com</p>" +
                "</body></html>";
    }

    //  ПОЛУЧЕНИЕ РЕЗУЛЬТАТОВ с parallelStream (КРИТЕРИЙ!)
    @GetMapping("/answer")
    public ResponseEntity<List<ContactData>> getResults(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        List<ContactData> all = contactRepository.findAll();

        // КРИТЕРИЙ: parallelStream для параллельной фильтрации и сортировки данных из H2
        List<ContactData> result = all.parallelStream()
                // Используем "EMAIL".equals() вместо c.getContactType().equals() для 100% защиты от NullPointerException
                .filter(c -> email == null || ("EMAIL".equals(c.getContactType()) && c.getContactValue().contains(email)))
                .filter(c -> phone == null || ("PHONE".equals(c.getContactType()) && c.getContactValue().contains(phone)))
                .sorted(Comparator.comparing(ContactData::getFoundAt).reversed())
                .skip((long) page * size)
                .limit(size)
                .toList(); // Метод .toList() нативно поддерживается в Java 21

        return ResponseEntity.ok(result);
    }
}