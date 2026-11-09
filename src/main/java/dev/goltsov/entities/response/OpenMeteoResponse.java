package dev.goltsov.entities.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public record OpenMeteoResponse(Current current) {
    public record Current(
            LocalDateTime time,
            Double temperature_2m,
            Double wind_speed_10m,
            Integer relative_humidity_2m
    ) {}
}
