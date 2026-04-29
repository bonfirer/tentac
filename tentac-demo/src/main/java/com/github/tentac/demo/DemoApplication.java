package com.github.tentac.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Demo application for Swagger2md.
 * Run this application and visit:
 * - http://localhost:8080/v2/markdown  (Markdown API documentation)
 * - http://localhost:8080/v2/llm-probe (LLM-optimized probe)
 * - http://localhost:8080/v2/llm-probe/json (JSON probe)
 */
@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
