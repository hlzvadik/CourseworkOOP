package dev.goltsov.entities.response;

import java.time.Instant;

public record CoinGeckoResponse(
        String name,
        Double current_price,
        Double market_cap,
        Double price_change_percentage_24h,
        Instant last_updated
) {}
