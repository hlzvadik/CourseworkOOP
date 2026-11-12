package dev.goltsov.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.goltsov.entities.agregate.AggregatedRecord;
import dev.goltsov.exception.StorageException;
import dev.goltsov.settings.WriteType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class CsvStorage implements Storage {
    private static final String[] HEADER = {"source", "params", "timestamp", "values"};

    private final ObjectMapper mapper;
    private final Path path;

    public CsvStorage(ObjectMapper mapper, Path path) {
        this.mapper = mapper;
        this.path = path;
    }

    @Override
    public void save(List<AggregatedRecord> records, WriteType writeType) {
        try {
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            boolean writeHeader = writeType == WriteType.NEW || !Files.exists(path) || Files.size(path) == 0;

            StandardOpenOption[] options = writeType == WriteType.NEW
                    ? new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE}
                    : new StandardOpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.APPEND};

            CSVFormat format = writeHeader
                    ? CSVFormat.DEFAULT.builder().setHeader(HEADER).build()
                    : CSVFormat.DEFAULT;

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, options);
                 CSVPrinter printer = new CSVPrinter(writer, format)) {
                for (AggregatedRecord r : records) {
                    printer.printRecord(
                            r.source(),
                            r.params(),
                            r.timestamp().toString(),
                            mapper.writeValueAsString(r.values()));
                }
            }
        } catch (IOException e) {
            throw new StorageException("Cannot write " + path + ": " + e.getMessage(), e);
        }
        log.info("Saved {} records to {} (mode={})", records.size(), path, writeType);
    }

    @Override
    public List<AggregatedRecord> readAll() {
        List<AggregatedRecord> result = new ArrayList<>();
        try {
            if (!Files.exists(path) || Files.size(path) == 0) {
                return result;
            }
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader(HEADER)
                    .setSkipHeaderRecord(true)
                    .build();
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8); CSVParser parser = format.parse(reader)) {
                for (CSVRecord row : parser) {
                    result.add(new AggregatedRecord(
                            row.get("source"),
                            row.get("params"),
                            Instant.parse(row.get("timestamp")),
                            mapper.readValue(row.get("values"), new TypeReference<Map<String, String>>() {
                            })));
                }
            }
        } catch (IOException | DateTimeParseException | IllegalArgumentException e) {
            throw new StorageException("Cannot read " + path + ": " + e.getMessage(), e);
        }
        log.info("Read {} records from {}", result.size(), path);
        return result;
    }
}
