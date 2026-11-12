package dev.goltsov.storage;

import dev.goltsov.entities.agregate.AggregatedRecord;
import dev.goltsov.settings.WriteType;

import java.util.List;

public interface Storage {
    void save(List<AggregatedRecord> records, WriteType writeType);
    List<AggregatedRecord> readAll();
}
