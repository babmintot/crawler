package com.example.crawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class ThreadPoolConfig {

    // КРИТЕРИЙ: Кастомный ExecutorService (не через Executors.newFixedThreadPool)
    @Bean(name = "crawlerExecutor")
    public ExecutorService crawlerExecutor() {
        AtomicInteger threadNumber = new AtomicInteger(1);
        ThreadFactory threadFactory = r -> {
            Thread t = new Thread(r, "Crawler-Worker-" + threadNumber.getAndIncrement());
            t.setDaemon(false); // Не демон-поток, чтобы приложение дождалось завершения задач
            return t;
        };

        return new ThreadPoolExecutor(
                5,                          // corePoolSize: базовое количество потоков
                15,                         // maximumPoolSize: максимальное количество при пиковой нагрузке
                60L, TimeUnit.SECONDS,      // keepAliveTime: время жизни лишних потоков
                new LinkedBlockingQueue<>(200), // workQueue: ограниченная очередь (защита от OOM)
                threadFactory,              // threadFactory: наша кастомная фабрика
                new ThreadPoolExecutor.CallerRunsPolicy() // rejectedExecutionHandler: если очередь полна, задача выполняется в потоке, который её отправил
        );
    }

    // КРИТЕРИЙ: WebClient (WebFlux) для межсервисного взаимодействия
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}