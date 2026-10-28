package com.example.crawler;

import com.example.crawler.service.ParsingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ParsingServiceTest {

    @Autowired
    private ParsingService parsingService;

    @Test
    void shouldExtractEmails() {
        String text = "Contact us at info@example.com or support@test.ru";
        Set<String> emails = parsingService.extractEmails(text);

        assertEquals(2, emails.size());
        assertTrue(emails.contains("info@example.com"));
    }

    @Test
    void shouldExtractPhones() {
        String text = "Call +7 (495) 123-45-67 or 8-800-555-35-35";
        Set<String> phones = parsingService.extractPhones(text);

        assertEquals(2, phones.size());
    }
}