package com.languagelearning.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public record StorageProperties(String basePath) {

    public StorageProperties {
        if (basePath == null || basePath.isBlank()) {
            basePath = "./data/storage";
        }
    }
}
