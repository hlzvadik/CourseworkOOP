package dev.goltsov.entities.agregate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.goltsov.entities.request.ApiForRequest;
import dev.goltsov.entities.request.HackerNewsApi;
import dev.goltsov.entities.response.HackerNewsResponse;
import dev.goltsov.exception.ApiException;
import dev.goltsov.http.HttpFetcher;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HackerNewsSource implements DataSource<HackerNewsResponse> {
    private final HttpFetcher http;
    private final ObjectMapper mapper;
    private final HackerNewsApi api;

    public HackerNewsSource(HttpFetcher http, ObjectMapper mapper, HackerNewsApi api) {
        this.http = http;
        this.mapper = mapper;
        this.api = api;
    }

    @Override
    public String name() {
        return "hackernews";
    }

    @Override
    public List<AggregatedRecord> fetch(Map<String, String> additionalParams) {
        Map<String, String> params = new LinkedHashMap<>(api.getVariables());
        params.putAll(additionalParams);
        String json = http.get(api.buildUrl(params));
        return map(parse(json), params);
    }

    @Override
    public HackerNewsResponse parse(String json) {
        try {
            HackerNewsResponse response = mapper.readValue(json, HackerNewsResponse.class);
            if (response == null) {
                throw new ApiException("Empty hackernews response");
            }
            return response;
        } catch (JsonProcessingException e) {
            throw new ApiException("Bad hackernews response", e);
        }
    }

    @Override
    public List<AggregatedRecord> map(HackerNewsResponse response, Map<String, String> params) {
        if (response.hits() == null) {
            throw new ApiException("No 'hits' in hackernews response");
        }
        String paramString = ApiForRequest.format(params);
        List<AggregatedRecord> result = new ArrayList<>();
        for (HackerNewsResponse.Hit hit : response.hits()) {
            Map<String, String> values = new LinkedHashMap<>();
            values.put("title", Objects.toString(hit.title(), ""));
            values.put("author", Objects.toString(hit.author(), ""));
            values.put("points", Objects.toString(hit.points(), ""));
            values.put("numComments", Objects.toString(hit.num_comments(), ""));
            Instant time = hit.created_at() != null ? hit.created_at() : Instant.now();
            result.add(new AggregatedRecord(name(), paramString, time, values));
        }
        return result;
    }
}
