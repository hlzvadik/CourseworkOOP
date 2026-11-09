package dev.goltsov.entities.agregate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.goltsov.entities.request.ApiForRequest;
import dev.goltsov.entities.request.HackerNewsApi;
import dev.goltsov.entities.response.HackerNewsResponse;
import dev.goltsov.exception.ApiException;
import dev.goltsov.http.HttpFetcher;

import java.util.*;

public class HackerNewsSource implements DataSource {
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
    public List<AggregatedRecord> fetch(Map<String, String> overrides) {
        Map<String, String> params = new LinkedHashMap<>(api.getVariables());
        params.putAll(overrides);
        String json = http.get(api.buildUrl(params));
        return map(parse(json), params);
    }

    HackerNewsResponse parse(String json) {
        try {
            return mapper.readValue(json, HackerNewsResponse.class);
        } catch (JsonProcessingException e) {
            throw new ApiException("Bad hackernews response", e);
        }
    }

    List<AggregatedRecord> map(HackerNewsResponse response, Map<String, String> params) {
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
            result.add(new AggregatedRecord(name(), paramString, hit.time(), values));
        }
        return result;
    }
}
