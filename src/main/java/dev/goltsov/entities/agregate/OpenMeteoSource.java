package dev.goltsov.entities.agregate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.goltsov.entities.request.ApiForRequest;
import dev.goltsov.entities.request.OpenMeteoApi;
import dev.goltsov.entities.response.OpenMeteoResponse;
import dev.goltsov.exception.ApiException;
import dev.goltsov.http.HttpFetcher;

import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class OpenMeteoSource implements DataSource<OpenMeteoResponse> {
    private final HttpFetcher http;
    private final ObjectMapper mapper;
    private final OpenMeteoApi api;

    public OpenMeteoSource(HttpFetcher http, ObjectMapper mapper, OpenMeteoApi api) {
        this.http = http;
        this.mapper = mapper;
        this.api = api;
    }

    @Override
    public String name() {
        return "weather";
    }

    @Override
    public List<AggregatedRecord> fetch(Map<String, String> additionalParams) {
        Map<String, String> params = new LinkedHashMap<>(api.getVariables());
        params.putAll(additionalParams);
        String json = http.get(api.buildUrl(params));
        return map(parse(json), params);
    }

    @Override
    public OpenMeteoResponse parse(String json) {
        try {
            OpenMeteoResponse response = mapper.readValue(json, OpenMeteoResponse.class);
            if (response == null) {
                throw new ApiException("Empty weather response");
            }
            return response;
        } catch (JsonProcessingException e) {
            throw new ApiException("Bad weather response", e);
        }
    }

    @Override
    public List<AggregatedRecord> map(OpenMeteoResponse response, Map<String, String> params) {
        OpenMeteoResponse.Current c = response.current();
        if (c == null || c.time() == null) {
            throw new ApiException("No 'current' in weather response");
        }
        Map<String, String> values = new LinkedHashMap<>();
        values.put("temperature", Objects.toString(c.temperature_2m(), ""));
        values.put("windSpeed", Objects.toString(c.wind_speed_10m(), ""));
        values.put("humidity", Objects.toString(c.relative_humidity_2m(), ""));
        return List.of(new AggregatedRecord(
                name(),
                ApiForRequest.format(params),
                c.time().toInstant(ZoneOffset.UTC),
                values));
    }
}
