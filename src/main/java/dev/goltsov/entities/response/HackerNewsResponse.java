package dev.goltsov.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record HackerNewsResponse(List<Hit> hits) implements Response {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Hit(
            Instant created_at,
            String title,
            String author,
            Integer points,
            Integer num_comments
    ) {}
}
