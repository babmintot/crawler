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





---

## 4. Схема работы приложения

**Этап 1: Инициация запроса.**  
Пользователь отправляет HTTP GET-запрос на эндпоинт `/api/crawl` с параметром `url`.

**Этап 2: Обработка контроллером.**  
`CrawlerController` принимает URL, вызывает метод `crawlerService.startCrawling(url)` и мгновенно возвращает ответ: *"Краулинг запущен. ID задачи: X"*.

**Этап 3: Многопоточная обработка (в фоне).**  
1. `CrawlerService` создает запись `CrawlTask` в БД H2 со статусом `RUNNING`.
2. Задача передается в кастомный `ThreadPoolExecutor` (настроенный в `ThreadPoolConfig`).
3. В выделенном потоке URL добавляется в `ConcurrentHashMap.newKeySet()` (защита от зацикливания).
4. `WebClient` асинхронно загружает HTML-код страницы (с таймаутом 5 секунд).
5. `ParsingService` извлекает Email и телефоны с помощью регулярных выражений и сохраняет их через JPA Repository.
6. Извлекаются внутренние ссылки, и для каждой в пул потоков отправляется новая задача.

**Этап 4: Получение результатов.**  
Пользователь отправляет GET-запрос на `/api/answer`. `CrawlerController` получает записи из БД и обрабатывает их через `parallelStream()`: фильтрует по типу, сортирует по дате (`.reversed()`), применяет пагинацию и возвращает чистый JSON (благодаря `@JsonIgnore`).

---

## 5. Инструкция по запуску и проверке

### ШАГ 1: Открытие проекта в IntelliJ IDEA
1. Запустите **IntelliJ IDEA**.
2. Нажмите **File** → **Open...** и найдите папку с проектом.
3. Дождитесь завершения импорта Maven (индикатор в правом нижнем углу исчезнет).

### ШАГ 2: Запуск приложения
1. В панели **Project** (слева) раскройте папку: `src/main/java/com/example/crawler`
2. Найдите файл **`CrawlerApplication.java`** и откройте его.
3. Слева от строки `public class CrawlerApplication` нажмите на **зеленую кнопку Play (▶)**.
4. Выберите **Run 'CrawlerApplication'**.

**Ожидаемый результат в консоли:**
```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.x.x)

... Started CrawlerApplication in 5.123 seconds
[2026-11-02T...] [SCHEDULED] Краулер активен. Проверка состояния системы...
