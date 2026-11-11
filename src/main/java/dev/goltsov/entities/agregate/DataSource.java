package dev.goltsov.entities.agregate;

import dev.goltsov.entities.request.ApiForRequest;
import dev.goltsov.entities.response.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public interface DataSource<T extends Response> {
    String name();
    List<AggregatedRecord> fetch(Map<String, String> overrides) throws URISyntaxException, IOException, InterruptedException;
    T parse(String json);
    List<AggregatedRecord> map(T response, Map<String, String> params);
}
