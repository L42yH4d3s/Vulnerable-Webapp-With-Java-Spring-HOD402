package com.example.webapp.config;

public class Test {
    public static void main(String[] args) {
        // Create DevPasswordEncoder instance
        DevPasswordEncoder encoder = new DevPasswordEncoder();
        String rawPassword = "123456";
        
        // Generate hash using DevPasswordEncoder
        String hash = encoder.encode(rawPassword);
        System.out.println("Generated hash using DevPasswordEncoder: " + hash);
        
        // Hash from database
        String dbHash = "$2a$10$fixedSaltForDevEnvironment.XJ6QFr2EwNTh3V0H0G/ku/WLkQQ2";
        System.out.println("DB hash: " + dbHash);
        
        // Check if they match using DevPasswordEncoder's matches method
        boolean matches = encoder.matches(rawPassword, dbHash);
        System.out.println("Password matches: " + matches);
    }
}