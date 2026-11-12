package dev.goltsov.entities.agregate;

import dev.goltsov.entities.response.Response;

import java.util.List;
import java.util.Map;

public interface DataSource<T extends Response> {
    String name();
    List<AggregatedRecord> fetch(Map<String, String> overrides);
    T parse(String json);
    List<AggregatedRecord> map(T response, Map<String, String> params);
}
