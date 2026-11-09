package dev.goltsov.entities;

import dev.goltsov.entities.agregate.AggregatedRecord;

import java.util.List;

public interface DataSource {
    List<AggregatedRecord> fetch(String params);
}
