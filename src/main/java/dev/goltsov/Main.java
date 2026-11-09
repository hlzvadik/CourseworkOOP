package dev.goltsov;

import dev.goltsov.settings.Settings;
import dev.goltsov.settings.SettingsParser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main {
    public static void main(String[] args) {
        SettingsParser settingsParser = new SettingsParser();
        Settings settings = null;
        try {
            settings = settingsParser.getSettings(args);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return;
        }


    }
}
