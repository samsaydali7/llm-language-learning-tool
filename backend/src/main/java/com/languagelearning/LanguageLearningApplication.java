package com.languagelearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LanguageLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(LanguageLearningApplication.class, args);
    }
}
