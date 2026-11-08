package dev.goltsov.settings;

import java.util.Arrays;

public class SettingsParser {

    public Settings getSettings(String[] args) {
        Settings settings = new Settings();
        boolean show = false;

        for (int i = 0; i < args.length; i++) {
            String key = args[i];
            switch (key) {
                case "--show" -> {
                    show = true;
                    if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                        settings.setShowSources(parseList(args[++i], key));
                    }
                }
                case "--sources" -> settings.setSources(parseList(nextValue(args, ++i, key), key));
                case "--fileType" -> {
                    try {
                        settings.setFileType(FileType.valueOf(nextValue(args, ++i, key).toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Incorrect fileType. Correct: 'json' or 'csv'");
                    }
                }
                case "--writeType" -> {
                    try {
                        settings.setWriteType(WriteType.valueOf(nextValue(args, ++i, key).toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Incorrect writeType. Correct: 'new' or 'append'");
                    }
                }
                default -> throw new IllegalArgumentException("Incorrect command line param: " + key);
            }
        }

        if (show) {
            if (settings.getSources() != null || settings.getWriteType() != null) {
                throw new IllegalArgumentException("--show cannot be combined with --sources or --writeType");
            }
            if (settings.getFileType() == null) {
                throw new IllegalArgumentException("Required with --show: --fileType");
            }
            settings.setAction(Action.SHOW);
        } else {
            if (settings.getSources() == null || settings.getFileType() == null || settings.getWriteType() == null) {
                throw new IllegalArgumentException("Required: --sources, --fileType, --writeType");
            }
        }
        return settings;
    }

    private String nextValue(String[] args, int index, String key) {
        if (index >= args.length || args[index].startsWith("--")) {
            throw new IllegalArgumentException("Missing value for " + key);
        }
        return args[index];
    }

    private String[] parseList(String value, String key) {
        String[] list = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
        if (list.length == 0) {
            throw new IllegalArgumentException("Empty list for " + key);
        }
        return list;
    }
}