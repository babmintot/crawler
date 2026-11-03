# Итоговое домашнее задание: Многопоточный краулер контактных данных (Вариант 1)

**Студент:** Петров Валерий Владимирович  
**Дисциплина:** Теория и практика многопоточности  
**Дата:** 02.11.2026  

---

## 1. Описание проекта
Приложение для автоматизированного многопоточного сбора контактных данных (Email, телефоны) с веб-сайтов. Сервис принимает стартовый URL, асинхронно обходит страницы, извлекает открытую информацию с помощью регулярных выражений и сохраняет её в базу данных. Проект демонстрирует современные практики конкурентного программирования в Java 21.

---

## 2. Стек технологий
- **Java 21** (использование современных фич, включая `.toList()`)
- **Spring Boot** (MVC архитектура, Dependency Injection)
- **Spring Data JPA** + **H2 Database** (встроенная in-memory база данных)
- **Spring WebFlux** (`WebClient` для неблокирующих асинхронных HTTP-запросов)
- **Jsoup** (парсинг HTML-документов и извлечение ссылок)
- **Lombok** (сокращение шаблонного кода)

---

## 3. Структура проекта

```text
crawler/
├── .mvn/wrapper/
│   └── maven-wrapper.properties      # Настройки Maven Wrapper
├── src/
│   ├── main/
│   │   ├── java/com/example/crawler/
│   │   │   ├── config/
│   │   │   │   └── ThreadPoolConfig.java          # Конфигурация кастомного ExecutorService и WebClient
│   │   │   ├── controller/
│   │   │   │   └── CrawlerController.java         # REST API эндпоинты (/crawl, /test-page, /answer)
│   │   │   ├── dto/
│   │   │   │   └── CrawlRequest.java              # DTO для POST-запроса
│   │   │   ├── entity/
│   │   │   │   ├── ContactData.java               # Entity: найденные контакты (Email, Phone)
│   │   │   │   └── CrawlTask.java                 # Entity: задача краулинга
│   │   │   ├── repository/
│   │   │   │   ├── ContactDataRepository.java     # JPA Repository для контактов
│   │   │   │   └── CrawlTaskRepository.java       # JPA Repository для задач
│   │   │   ├── service/
│   │   │   │   ├── CrawlerService.java            # Бизнес-логика: многопоточность, WebClient, ConcurrentHashMap
│   │   │   │   ├── ParsingService.java            # Парсинг: Regex для Email/телефонов, Jsoup для ссылок
│   │   │   │   └── ScheduledTasks.java            # Фоновая задача @Scheduled (каждые 60 сек)
│   │   │   └── CrawlerApplication.java            # ГЛАВНЫЙ КЛАСС: точка входа (@SpringBootApplication)
│   │   └── resources/
│   │       └── application.properties             # Конфигурация: порт 8080, H2 база, JPA настройки
│   └── test/java/com/example/crawler/
│       └── CrawlerApplicationTests.java           # Unit тесты
├── .gitattributes                                 # Настройки Git
├── .gitignore                                     # Игнорируемые файлы (target, .idea, и т.д.)
├── README.md                                      # Этот файл: документация проекта
└── pom.xml                                        # Maven конфигурация: зависимости, версия Java 21
