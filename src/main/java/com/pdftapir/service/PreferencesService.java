package com.pdftapir.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Persistent user preferences backed by {@code ~/.pdftapir/preferences.properties}.
 * The directory is created on first write if it does not exist.
 */
public class PreferencesService {

    private static final Path PREFS_DIR  = Path.of(System.getProperty("user.home"), ".pdftapir");
    private static final Path PREFS_FILE = PREFS_DIR.resolve("preferences.properties");

    private static final String KEY_SCROLL_PAN = "scroll.pan";

    private final Properties props = new Properties();

    public PreferencesService() {
        load();
    }

    public void load() {
        if (Files.exists(PREFS_FILE)) {
            try (InputStream in = Files.newInputStream(PREFS_FILE)) {
                props.load(in);
            } catch (IOException ignored) {
                // Use defaults if file is unreadable
            }
        }
    }

    public void save() {
        try {
            if (!Files.exists(PREFS_DIR)) {
                Files.createDirectories(PREFS_DIR);
            }
            try (OutputStream out = Files.newOutputStream(PREFS_FILE)) {
                props.store(out, "PDF Tapir preferences");
            }
        } catch (IOException ignored) {
            // Best-effort save
        }
    }

    public boolean isScrollPan() {
        return Boolean.parseBoolean(props.getProperty(KEY_SCROLL_PAN, "false"));
    }

    public void setScrollPan(boolean enabled) {
        props.setProperty(KEY_SCROLL_PAN, Boolean.toString(enabled));
    }
}
