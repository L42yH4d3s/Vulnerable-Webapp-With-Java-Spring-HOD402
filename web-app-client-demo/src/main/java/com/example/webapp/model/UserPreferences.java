package com.example.webapp.model;

import lombok.Data;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Plain-old Java object (POJO) used for exporting / importing user interface
 * preferences.  NO persistence annotations are present – the object is **not**
 * mapped to any database table.  The sole purpose is (de)serialization, which
 * we will intentionally make vulnerable in an import endpoint for demo.
 */
@Data
public class UserPreferences implements Serializable {

    private static final long serialVersionUID = 1L;

    /* The user this preference set belongs to */
    private Long userId;

    /* UI preferences */
    private String theme = "default";           // e.g. light / dark
    private String language = "en";            // i18n code
    private Boolean emailNotifications = true;  // receive promo mails?

    /* Extensible map for arbitrary settings */
    private Map<String, Object> custom = new HashMap<>();

    public UserPreferences() {
    }

    public UserPreferences(Long userId) {
        this.userId = userId;
    }

    /**
     * Method called after the preferences are deserialized and applied.  In a
     * real-world app this may update session/theme.  Here it just prints to
     * stdout – but an attacker's gadget chain could hijack this execution.
     */
    public void apply() {
        System.out.println("[UserPreferences] Applying settings for user " + userId);
    }
} 