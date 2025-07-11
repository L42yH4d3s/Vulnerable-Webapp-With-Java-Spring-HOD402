package com.example.webapp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "test")
public class TestProperties {
    private Admin admin = new Admin();
    private User user = new User();

    @Data
    public static class Admin {
        private String username;
        private String password;
    }

    @Data
    public static class User {
        private String username;
        private String password;
    }
}