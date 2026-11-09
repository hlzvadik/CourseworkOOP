package dev.goltsov.entities.agregate;

import java.util.List;
import java.util.Map;

public interface DataSource {
    String name();
    List<AggregatedRecord> fetch(Map<String, String> overrides);
}
