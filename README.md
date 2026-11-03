# Итоговое домашнее задание: Многопоточный краулер контактных данных (Вариант 1)

**Студент:** Петров Валерий Владимирович  
**Дисциплина:** Теория и практика многопоточности  
**Дата:** 02.11.2026  

---

## 1. Описание проекта
Приложение для автоматизированного многопоточного сбора контактных данных (Email, телефоны) с веб-сайтов. Сервис принимает стартовый URL, асинхронно обходит страницы, извлекает открытую информацию с помощью регулярных выражений и сохраняет её в базу данных. Проект демонстрирует современные практики конкурентного программирования в Java 21.

---

## 2. Стек технологий
- **Java 21** (использование современных фич, включая `.toList()` и API виртуальных потоков)
- **Spring Boot** (MVC архитектура, Dependency Injection)
- **Spring Data JPA** + **H2 Database** (встроенная in-memory база данных для хранения результатов)
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

## 4. Схема работы приложения (Пошаговый поток данных)
Этап 1: Инициация запроса

Пользователь отправляет HTTP GET-запрос на эндпоинт /api/crawl с параметром url.

Этап 2: Обработка контроллером

CrawlerController принимает URL, вызывает метод crawlerService.startCrawling(url) и мгновенно возвращает ответ: "Краулинг запущен. ID задачи: X".

Этап 3: Многопоточная обработка (в фоне)

CrawlerService создает запись CrawlTask в базе данных H2 со статусом RUNNING.

Задача передается в кастомный ThreadPoolExecutor (настроенный в ThreadPoolConfig).

В выделенном потоке выполняется метод processUrl:

URL добавляется в ConcurrentHashMap.newKeySet() (гарантия потокобезопасности и защита от зацикливания).

WebClient асинхронно загружает HTML-код страницы (с таймаутом 5 секунд).

ParsingService извлекает Email и телефоны с помощью регулярных выражений.

Найденные данные сохраняются в таблицу contact_data через JPA Repository.

ParsingService извлекает все внутренние ссылки со страницы, и для каждой из них в пул потоков отправляется новая задача processUrl.

Этап 4: Получение результатов

Пользователь отправляет GET-запрос на /api/answer.

CrawlerController получает все записи из БД и обрабатывает их с помощью parallelStream():

Фильтрует по типу контакта (если указан параметр type).

Сортирует по дате нахождения в обратном порядке (.reversed()).

Применяет пагинацию (.skip() и .limit()).

Возвращает чистый JSON-массив без циклических ссылок (благодаря @JsonIgnore).


## 5. ПОЛНАЯ ИНСТРУКЦИЯ ПО ЗАПУСКУ И ПРОВЕРКЕ (ПОШАГОВО С СКРИНШОТАМИ)

### ШАГ 1: Открытие проекта в IntelliJ IDEA

**Что делать:**
1. Запустите **IntelliJ IDEA**.
2. Нажмите **File** → **Open...** (или **Open Project**).
3. Найдите папку с проектом (например, `D:\crawler`) и нажмите **OK**.
4. Дождитесь завершения импорта Maven (индикатор в правом нижнем углу исчезнет).

**Что вы увидите:**
- Слева откроется панель **Project** со структурой файлов (как на скриншоте выше).
- Внизу появится вкладка **Maven** со списком зависимостей.

---

### ШАГ 2: Запуск приложения

**Что делать:**
1. В панели **Project** (слева) раскройте папку:  
   `src/main/java/com.example.crawler`
2. Найдите файл **`CrawlerApplication.java`** (главный класс).
3. Откройте его двойным кликом.
4. Слева от строки `public class CrawlerApplication` нажмите на **зеленую кнопку Play (▶)**.
5. Выберите **Run 'CrawlerApplication'**.

**ОЖИДАЕМЫЙ РЕЗУЛЬТАТ В КОНСОЛИ (вкладка Run внизу):**
```text
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.x.x)

2026-11-02T...  INFO  ... --- [main] c.e.c.CrawlerApplication   : Starting CrawlerApplication using Java 21.x.x
2026-11-02T...  INFO  ... --- [main] c.e.c.CrawlerApplication   : No active profile set, falling back to 1 default profile: "default"
2026-11-02T...  INFO  ... --- [main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories...
2026-11-02T...  INFO  ... --- [main] o.s.b.w.embedded.tomcat.TomcatWebServer : Tomcat started on port 8080 (http)
2026-11-02T...  INFO  ... --- [main] c.e.c.CrawlerApplication   : Started CrawlerApplication in 5.123 seconds

[2026-11-02T...] [SCHEDULED] Краулер активен. Проверка состояния системы...
