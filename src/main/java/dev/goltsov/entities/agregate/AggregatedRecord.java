package dev.goltsov.entities.agregate;

import java.time.Instant;
import java.util.Map;

public record AggregatedRecord(
        String source,
        String params,
        Instant timestamp,
        Map<String, String> values
) {
}
