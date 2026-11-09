package dev.goltsov.entities.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;

public record HackerNewsResponse(List<Hit> hits) {
    public record Hit(
            @JsonProperty("created_at") Instant time,
            String title,
            String author,
            Integer points,
            Integer num_comments
    ) {}
}
