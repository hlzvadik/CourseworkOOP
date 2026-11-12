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
                case "--fileType" -> settings.setFileType(
                        parseEnum(FileType.class, nextValue(args, ++i, key), key, "'json' or 'csv'"));
                case "--writeType" -> settings.setWriteType(
                        parseEnum(WriteType.class, nextValue(args, ++i, key), key, "'new' or 'append'"));
                case "--outputFile" -> settings.setOutputFile(nextValue(args, ++i, key));
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

        if (settings.getOutputFile() == null) {
            settings.setOutputFile("data." + settings.getFileType().name().toLowerCase());
        }
        return settings;
    }

    private String nextValue(String[] args, int index, String key) {
        if (index >= args.length || args[index].startsWith("--")) {
            throw new IllegalArgumentException("Missing value for " + key);
        }
        return args[index];
    }

    private <E extends Enum<E>> E parseEnum(Class<E> type, String value, String key, String allowed) {
        try {
            return Enum.valueOf(type, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Incorrect " + key + ": " + value + ". Correct: " + allowed);
        }
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
