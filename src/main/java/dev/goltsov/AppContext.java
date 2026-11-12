package dev.goltsov;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.goltsov.entities.agregate.CoinGeckoSource;
import dev.goltsov.entities.agregate.DataSource;
import dev.goltsov.entities.agregate.HackerNewsSource;
import dev.goltsov.entities.agregate.OpenMeteoSource;
import dev.goltsov.entities.request.CoinGeckoApi;
import dev.goltsov.entities.request.HackerNewsApi;
import dev.goltsov.entities.request.OpenMeteoApi;
import dev.goltsov.http.HttpApiClient;
import dev.goltsov.http.HttpFetcher;
import dev.goltsov.settings.Settings;
import dev.goltsov.storage.CsvStorage;
import dev.goltsov.storage.JsonStorage;
import dev.goltsov.storage.Storage;
import lombok.Getter;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class AppContext {

    private final Settings settings;
    private final ObjectMapper objectMapper;
    private final HttpApiClient httpApiClient;
    private final HttpFetcher httpFetcher;
    private final Storage storage;
    private final Map<String, DataSource<?>> sources;
    private final Aggregator aggregator;

    public AppContext(Settings settings) {
        this.settings = settings;
        this.objectMapper = createMapper();
        this.httpApiClient = createHttpApiClient();
        this.httpFetcher = createHttpFetcher(this.httpApiClient);
        this.storage = createStorage(this.settings, this.objectMapper);
        this.sources = createSources(this.httpFetcher, this.objectMapper);
        this.aggregator = createAggregator(sources, objectMapper, settings);
    }

    private ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    private HttpApiClient createHttpApiClient() {
        return new HttpApiClient();
    }

    private HttpFetcher createHttpFetcher(HttpApiClient httpApiClient) {
        return new HttpFetcher(httpApiClient);
    }

    private Storage createStorage(Settings settings, ObjectMapper mapper) {
        Path path = Path.of(settings.getOutputFile());
        return switch (settings.getFileType()) {
            case JSON -> new JsonStorage(mapper, path);
            case CSV -> new CsvStorage(mapper, path);
        };
    }

    private Map<String, DataSource<?>> createSources(HttpFetcher httpFetcher, ObjectMapper objectMapper) {
        Map<String, DataSource<?>> sources = new LinkedHashMap<>();
        register(sources, new OpenMeteoSource(httpFetcher, objectMapper, new OpenMeteoApi()));
        register(sources, new HackerNewsSource(httpFetcher, objectMapper, new HackerNewsApi()));
        register(sources, new CoinGeckoSource(httpFetcher, objectMapper, new CoinGeckoApi()));
        return sources;
    }

    private Aggregator createAggregator(Map<String, DataSource<?>> sources, ObjectMapper objectMapper, Settings settings) {
        return new Aggregator(sources, createStorage(settings, objectMapper));
    }

    private void register(Map<String, DataSource<?>> sources, DataSource<?> source) {
        sources.put(source.name(), source);
    }
}
