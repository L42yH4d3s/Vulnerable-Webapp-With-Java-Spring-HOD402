package com.example.webapp.controller;

import com.example.webapp.model.Role;
import com.example.webapp.model.User;
import com.example.webapp.model.UserPreferences;
import com.example.webapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "users/list";
    }
    
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", Role.values());
        return "users/form";
    }
    
    @PostMapping("/add")
    public String addUser(@ModelAttribute User user) {
        userService.saveUser(user);
        return "redirect:/users";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("roles", Role.values());
            return "users/form";
        }
        return "redirect:/users";
    }
    
    @PostMapping("/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        User existingUser = userService.getUserById(id);
        if (existingUser != null) {
            user.setId(id);
            userService.saveUser(user);
        }
        return "redirect:/users";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/users";
    }
    
    // SECURE: User search using safe JPA repository methods
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<?> searchUser(@RequestParam String username) {
        try {
            // Input validation
            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            
            if (username.trim().length() > 50) {
                return ResponseEntity.badRequest().body("Username too long");
            }
            
            // SECURE: Using safe JPA repository method
            User user = userService.findByUsernameSafe(username.trim());
            if (user != null) {
                return ResponseEntity.ok(user);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Search failed");
        }
    }

    /* ======== Preference EXPORT ======== */
    @GetMapping("/{id}/preferences/export")
    @ResponseBody
    public String exportPreferences(@PathVariable("id") Long id) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return "Error: user not found";
            }
            UserPreferences prefs = new UserPreferences(id);
            prefs.setTheme("default");
            prefs.setLanguage("en");
            // -- serialize --
            return userService.serializePreferences(prefs);
        } catch (Exception e) {
            return "Export failed: " + e.getMessage();
        }
    }

    /* ======== Preference IMPORT – VULNERABLE ======== */
    @PostMapping("/{id}/preferences/import")
    @ResponseBody
    public String importPreferences(@PathVariable("id") Long id,
                                    @RequestParam("data") String base64) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                return "Error: user not found";
            }
            // VULNERABLE deserialization delegated to service layer
            Object obj = userService.deserializePreferences(base64);

            // If the payload is a legitimate UserPreferences instance, apply it.
            if (obj instanceof UserPreferences) {
                UserPreferences prefs = (UserPreferences) obj;
                prefs.apply();
                return "Preferences imported for user " + user.getUsername();
            }

            // Otherwise just report what we deserialized (RCE already occurred inside readObject)
            return "Deserialized: " + (obj != null ? obj.toString() : "null");
        } catch (Exception e) {
            return "Import failed: " + e.getMessage();
        }
    }

    /* ======== SAMPLE PAYLOAD (benign) ======== */
    @GetMapping("/preferences/sample")
    @ResponseBody
    public String samplePreferences(@RequestParam(value = "userId", defaultValue = "1") Long userId) {
        try {
            UserPreferences p = new UserPreferences(userId);
            p.setTheme("dark");
            p.setLanguage("vi");
            Map<String, Object> m = new HashMap<>();
            m.put("sidebar", "collapsed");
            p.setCustom(m);
            return userService.serializePreferences(p);
        } catch (Exception e) {
            return "Failed: " + e.getMessage();
        }
    }

    /* ======== CHECK for CC gadget vulnerability ======== */
    @GetMapping("/preferences/vuln-info")
    @ResponseBody
    public String checkDeserializationVulnerability() {
        try {
            Class.forName("org.apache.commons.collections.functors.InvokerTransformer");
            Package cc = Package.getPackage("org.apache.commons.collections");
            String ver = cc != null ? cc.getImplementationVersion() : "unknown";
            return "Commons-Collections " + ver + " present. Import endpoint is vulnerable to RCE via crafted serialized objects.";
        } catch (ClassNotFoundException e) {
            return "Commons-Collections not on classpath – gadget chain not available (but deserialization still risky).";
        }
    }
} 