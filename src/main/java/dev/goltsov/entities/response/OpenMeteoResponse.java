package dev.goltsov.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenMeteoResponse(Current current) implements Response {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Current(
            LocalDateTime time,
            Double temperature_2m,
            Double wind_speed_10m,
            Integer relative_humidity_2m
    ) {}
}
