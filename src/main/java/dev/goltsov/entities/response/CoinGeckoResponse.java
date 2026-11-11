package dev.goltsov.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoinGeckoResponse(List<Coin> coins) implements Response {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Coin(
            String name,
            Double current_price,
            Double market_cap,
            Double price_change_percentage_24h,
            Instant last_updated
    ) {
    }
}
