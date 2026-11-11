package dev.goltsov;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.goltsov.entities.agregate.CoinGeckoSource;
import dev.goltsov.entities.agregate.HackerNewsSource;
import dev.goltsov.entities.agregate.OpenMeteoSource;
import dev.goltsov.entities.request.CoinGeckoApi;
import dev.goltsov.entities.request.HackerNewsApi;
import dev.goltsov.entities.request.OpenMeteoApi;
import dev.goltsov.http.HttpFetcher;
import dev.goltsov.settings.Settings;
import dev.goltsov.settings.SettingsParser;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.Map;

@Slf4j
public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        SettingsParser settingsParser = new SettingsParser();
        Settings settings = null;
        try {
            settings = settingsParser.getSettings(args);
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return;
        }

        HttpFetcher httpFetcher = new HttpFetcher();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES);

        Aggregator aggregator = new Aggregator(Map.of(
                    "weather", new OpenMeteoSource(httpFetcher, objectMapper, new OpenMeteoApi()),
                    "hackernews", new HackerNewsSource(httpFetcher, objectMapper, new HackerNewsApi()),
                    "crypto", new CoinGeckoSource(httpFetcher, objectMapper, new CoinGeckoApi())
                ),
                objectMapper,
                settings
        );
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
        aggregator.run(writer);
        writer.flush();
        writer.close();
    }
}
