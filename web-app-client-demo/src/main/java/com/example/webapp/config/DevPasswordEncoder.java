package com.example.webapp.config;

import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@Profile("dev")
public class DevPasswordEncoder implements PasswordEncoder {
    private static final String FIXED_SALT = "$2a$10$fixedSaltForDevEnvironm"; // đúng 22 ký tự


    @Override
    public String encode(CharSequence rawPassword) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(rawPassword.toString(), FIXED_SALT);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        String hash = org.mindrot.jbcrypt.BCrypt.hashpw(rawPassword.toString(), FIXED_SALT);
        return hash.equals(encodedPassword);
    }
}
