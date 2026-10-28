package com.example.crawler.dto;

import lombok.Data;

@Data
public class CrawlRequest {
    private String url;
    private int maxDepth;
}