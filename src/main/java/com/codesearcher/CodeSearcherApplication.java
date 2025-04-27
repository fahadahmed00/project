package com.codesearcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CodeSearcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeSearcherApplication.class, args);
    }
}