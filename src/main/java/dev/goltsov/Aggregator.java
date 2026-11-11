package dev.goltsov;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.goltsov.entities.agregate.AggregatedRecord;
import dev.goltsov.entities.agregate.DataSource;
import dev.goltsov.settings.Action;
import dev.goltsov.settings.FileType;
import dev.goltsov.settings.Settings;
import dev.goltsov.storage.CsvStorage;
import dev.goltsov.storage.JsonStorage;
import dev.goltsov.storage.Storage;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Aggregator {
    private final Map<String, DataSource<?>> sources;
    private final ObjectMapper objectMapper;
    private final Settings settings;

    public Aggregator(Map<String, DataSource<?>> sources, ObjectMapper objectMapper, Settings settings) {
        this.sources = sources;
        this.settings = settings;
        this.objectMapper = objectMapper;
    }

    public void run(Writer writer) throws URISyntaxException, IOException, InterruptedException {
        if (settings.getAction() == Action.POLL) {
            Map<String, List<AggregatedRecord>> map = pollAll();
            List<AggregatedRecord> res = new ArrayList<>();
            for (List<AggregatedRecord> e: map.values()) {
                res.addAll(e);
            }
            save(res);
        } else {
            List<AggregatedRecord> result = new ArrayList<>();
            for (List<AggregatedRecord> i: readAll().values()) {
                result.addAll(i);
            }
            writer.write(result.toString());
        }
    }

    public Map<String, List<AggregatedRecord>> pollAll() throws URISyntaxException, IOException, InterruptedException {
        Map<String, List<AggregatedRecord>> result = new HashMap<>();
        for (String key : settings.getSources()) {
            result.put(key, poll(key));
        }
        return result;
    }

    public List<AggregatedRecord> poll(String name) throws URISyntaxException, IOException, InterruptedException {
        if (!sources.containsKey(name)) {
            throw new IllegalArgumentException("No source with name " + name);
        }
        return sources.get(name).fetch(new HashMap<>());
    }

    public Map<String, List<AggregatedRecord>> readAll() throws URISyntaxException {
        Map<String, List<AggregatedRecord>> result = new HashMap<>();
        for (String source : settings.getShowSources()) {
            result.put(source, read(source));
        }
        return result;
    }

    public List<AggregatedRecord> read(String name) throws URISyntaxException {
        if (!contains(settings.getShowSources(), name)) {
            throw new IllegalArgumentException("No source with name " + name);
        }
        Path path = Path.of(new URI(name));
        Storage storage1;
        if (name.endsWith(".json")) {
            storage1 = new JsonStorage(objectMapper, path);
        } else if (name.endsWith(".csv")) {
            storage1 = new CsvStorage(objectMapper, path);
        } else {
            throw new IllegalArgumentException("Unknown file-type");
        }
        return storage1.readAll();
    }

    public void save(List<AggregatedRecord> data) {
        Storage storage;
        if (settings.getFileType() == FileType.JSON) {
            storage = new JsonStorage(objectMapper, Path.of(settings.getOutputFile()));
        } else if (settings.getFileType() == FileType.CSV) {
            storage = new CsvStorage(objectMapper, Path.of(settings.getOutputFile()));
        } else {
            throw new IllegalArgumentException("No such file type");
        }
        storage.save(data, settings.getWriteType());
    }

    private boolean contains(String[] array, String value) {
        for (String s : array) {
            if (s.equals(value)) return true;
        }
        return false;
    }
}
