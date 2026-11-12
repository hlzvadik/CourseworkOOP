package dev.goltsov;

import dev.goltsov.entities.agregate.AggregatedRecord;
import dev.goltsov.entities.agregate.DataSource;
import dev.goltsov.exception.ApiException;
import dev.goltsov.settings.WriteType;
import dev.goltsov.storage.Storage;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Slf4j
public class Aggregator {
    private final Map<String, DataSource<?>> sources;
    private final Storage storage;

    public Aggregator(Map<String, DataSource<?>> sources, Storage storage) {
        this.sources = sources;
        this.storage = storage;
    }

    public PollResult poll(List<String> names, WriteType writeType) {
        List<String> unique = new ArrayList<>(new LinkedHashSet<>(names));
        validate(unique);

        List<AggregatedRecord> all = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (String name : unique) {
            log.info("[START] {}", name);
            try {
                List<AggregatedRecord> records = sources.get(name).fetch(Map.of());
                all.addAll(records);
                log.info("[END] {} OK records={}", name, records.size());
            } catch (ApiException e) {
                errors.add(name + ": " + e.getMessage());
                log.error("[END] {} ERROR {}", name, e.getMessage());
            }
        }

        if (all.isEmpty()) {
            log.warn("Nothing to save, file is not changed");
        } else {
            storage.save(all, writeType);
        }
        return new PollResult(all.size(), errors);
    }

    public List<AggregatedRecord> read(List<String> names) {
        validate(names);
        List<AggregatedRecord> all = storage.readAll();
        if (names.isEmpty()) {
            return all;
        }
        return all.stream()
                .filter(r -> names.contains(r.source()))
                .toList();
    }

    private void validate(List<String> names) {
        for (String name : names) {
            if (!sources.containsKey(name)) {
                throw new IllegalArgumentException(
                        "Unknown source: " + name + ". Available: " + sources.keySet());
            }
        }
    }
}
