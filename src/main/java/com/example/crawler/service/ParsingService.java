package com.example.crawler.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParsingService {

    // Регулярные выражения для поиска email и российских номеров телефонов
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(\\+7|8)[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}");

    public Set<String> extractEmails(String html) {
        return extractMatches(html, EMAIL_PATTERN);
    }

    public Set<String> extractPhones(String html) {
        return extractMatches(html, PHONE_PATTERN);
    }

    private Set<String> extractMatches(String text, Pattern pattern) {
        Set<String> matches = new HashSet<>();
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches;
    }

    //  НОВЫЙ МЕТОД: Парсим уже полученный HTML, не делая новый запрос в сеть
    public Document parseHtml(String html) {
        return Jsoup.parse(html);
    }

    public Set<String> extractLinks(Document doc, String baseUrl) {
        Set<String> links = new HashSet<>();
        for (Element link : doc.select("a[href]")) {
            String absUrl = link.absUrl("href");
            // Ограничиваемся тем же доменом, чтобы краулер не ушел на внешние сайты бесконечно
            if (absUrl.startsWith(baseUrl) && !absUrl.equals(baseUrl)) {
                links.add(absUrl);
            }
        }
        return links;
    }
}