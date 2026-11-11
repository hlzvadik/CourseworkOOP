package dev.goltsov.entities.response;

import java.time.Instant;
import java.util.List;

public record CoinGeckoResponse(List<Coin> coins) implements Response {
    public static record Coin(
            String name,
            Double current_price,
            Double market_cap,
            Double price_change_percentage_24h,
            Instant last_updated
    ) {
    }
}
