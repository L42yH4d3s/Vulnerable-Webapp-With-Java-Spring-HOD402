package com.example.webapp.util;

import org.mindrot.jbcrypt.BCrypt;

public class HashTest {
    public static void main(String[] args) {
        // Sử dụng đúng salt này (22 ký tự base64 + prefix $2a$10$)
        String salt = "$2a$10$fixedSaltForDevEnvironm";
        String[] passwords = {"admin", "seller1", "seller2", "seller3", "user", "user2", "user3"};
        for (String pwd : passwords) {
            String hash = BCrypt.hashpw(pwd, salt);
            System.out.println(pwd + " -> " + hash);
        }
    }
}
