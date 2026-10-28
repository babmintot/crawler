package com.example.crawler.service;

import com.example.crawler.entity.ContactData;
import com.example.crawler.entity.CrawlTask;
import com.example.crawler.repository.ContactDataRepository;
import com.example.crawler.repository.CrawlTaskRepository;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Service
public class CrawlerService {
    private final CrawlTaskRepository taskRepository;
    private final ContactDataRepository contactRepository;
    private final ParsingService parsingService;
    private final WebClient webClient;
    private final ExecutorService executorService;

    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();

    public CrawlerService(CrawlTaskRepository taskRepository,
                          ContactDataRepository contactRepository,
                          ParsingService parsingService,
                          WebClient.Builder webClientBuilder,
                          @Qualifier("crawlerExecutor") ExecutorService executorService) {
        this.taskRepository = taskRepository;
        this.contactRepository = contactRepository;
        this.parsingService = parsingService;
        this.webClient = webClientBuilder.build();
        this.executorService = executorService;
    }

    public Long startCrawling(String startUrl) {
        CrawlTask newTask = new CrawlTask();
        newTask.setStartUrl(startUrl);
        newTask.setStatus(CrawlTask.Status.RUNNING);
        newTask.setCreatedAt(LocalDateTime.now());
        CrawlTask savedTask = taskRepository.save(newTask);

        System.out.println(">>>  ЗАПУСК КРАУЛИНГА для URL: " + startUrl);
        executorService.submit(() -> processUrl(startUrl, savedTask));
        return savedTask.getId();
    }

    private void processUrl(String url, CrawlTask task) {
        if (!visitedUrls.add(url)) {
            System.out.println(">>> ️ URL уже посещен, пропускаем: " + url);
            return;
        }

        System.out.println(">>> 1) Начинаем обработку URL: " + url);
        try {
            System.out.println(">>> 2) Делаем запрос через WebClient...");
            String html = webClient.get().uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .onErrorReturn("ERROR")
                    .block();

            if (html == null || "ERROR".equals(html)) {
                System.out.println(">>>  ОШИБКА: Не удалось получить HTML или таймаут для " + url);
                return;
            }
            System.out.println(">>> 3) HTML получен успешно. Длина: " + html.length() + " символов.");

            // Важно: используем parseHtml, а не fetchDocument!
            Document doc = parsingService.parseHtml(html);

            Set<String> emails = parsingService.extractEmails(html);
            System.out.println(">>> 4) Найдено email-адресов: " + emails.size() + " -> " + emails);

            Set<String> phones = parsingService.extractPhones(html);
            System.out.println(">>> 5) Найдено телефонов: " + phones.size() + " -> " + phones);

            for (String email : emails) {
                saveContact(task, url, "EMAIL", email);
            }
            for (String phone : phones) {
                saveContact(task, url, "PHONE", phone);
            }

            Set<String> links = parsingService.extractLinks(doc, url);
            System.out.println(">>> 6) Найдено внутренних ссылок для дальнейшего обхода: " + links.size());

            System.out.println(">>> 7) Обработка URL завершена успешно: " + url);

        } catch (Exception e) {
            System.err.println(">>>  КРИТИЧЕСКАЯ ОШИБКА при обработке URL " + url + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveContact(CrawlTask task, String sourceUrl, String contactType, String contactValue) {
        System.out.println(">>> 8) Сохраняем в БД: " + contactType + " = " + contactValue);
        ContactData contact = new ContactData();
        contact.setTask(task);
        contact.setSourceUrl(sourceUrl);
        contact.setContactType(contactType);
        contact.setContactValue(contactValue);
        contact.setFoundAt(LocalDateTime.now());
        contactRepository.save(contact);
        System.out.println(">>> 9 Успешно сохранено в БД!");
    }
}