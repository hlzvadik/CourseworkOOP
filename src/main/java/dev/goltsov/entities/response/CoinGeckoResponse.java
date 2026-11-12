package dev.goltsov.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CoinGeckoResponse(List<Coin> coins) implements Response {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Coin(
            String name,
            BigDecimal current_price,
            BigDecimal market_cap,
            Double price_change_percentage_24h,
            Instant last_updated
    ) {
    }
}
