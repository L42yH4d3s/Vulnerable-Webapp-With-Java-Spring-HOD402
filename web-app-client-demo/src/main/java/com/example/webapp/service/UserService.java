package com.example.webapp.service;

import com.example.webapp.model.User;
import com.example.webapp.model.UserPreferences;
import com.example.webapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.Base64;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public User saveUser(User user) {
        if (user.getId() == null) {
            // New user - encode password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // Existing user - only encode password if it has changed
            User existingUser = getUserById(user.getId());
            if (existingUser != null && !user.getPassword().equals(existingUser.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // SECURE: Safe username search using JPA repository
    public User findByUsernameSafe(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return userRepository.findByUsername(username.trim());
    }
    
    public User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return findByUsername(authentication.getName());
    }
    
    /* -------------------------------------------------
     *  (Insecure) Serialization helpers for demo
     * ------------------------------------------------- */

    /**
     * Serialize a UserPreferences object to Base64 string.
     */
    public String serializePreferences(UserPreferences prefs) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(prefs);
            return Base64.getEncoder().encodeToString(bos.toByteArray());
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Vulnerable deserialization helper – returns UserPreferences without any
     * validation.  Used by controller to illustrate the exploit.
     */
    @SuppressWarnings("unchecked")
    public <T> T deserializePreferences  (String base64) {
        try {
            byte[] raw = Base64.getDecoder().decode(base64);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(raw));
            Object obj = ois.readObject();
            System.out.println("Deserialized object: " + obj);
            return (T) obj;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
} 