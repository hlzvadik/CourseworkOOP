package dev.goltsov.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.goltsov.entities.agregate.AggregatedRecord;
import dev.goltsov.exception.StorageException;
import dev.goltsov.settings.WriteType;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JsonStorage implements Storage {
    private final ObjectMapper objectMapper;
    private final Path path;

    public JsonStorage(ObjectMapper objectMapper, Path path) {
        this.objectMapper = objectMapper;
        this.path = path;
    }

    @Override
    public void save(List<AggregatedRecord> records, WriteType writeType) {
        List<AggregatedRecord> all = new ArrayList<>();
        if (writeType == WriteType.APPEND) {
            all.addAll(readAll());
        }
        all.addAll(records);
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), all);
        } catch (IOException e) {
            throw new StorageException("Cannot write " + path + ": " + e.getMessage(), e);
        }
        log.info("Saved {} records to {} (mode={}, total in file={})", records.size(), path, writeType, all.size());
    }

    @Override
    public List<AggregatedRecord> readAll() {
        try {
            if (!Files.exists(path) || Files.size(path) == 0) {
                return new ArrayList<>();
            }
            List<AggregatedRecord> result = objectMapper.readValue(path.toFile(), new TypeReference<List<AggregatedRecord>>() {});
            log.info("Read {} records from {}", result.size(), path);
            return result;
        } catch (IOException e) {
            throw new StorageException("Cannot read " + path + ": " + e.getMessage(), e);
        }
    }
}
