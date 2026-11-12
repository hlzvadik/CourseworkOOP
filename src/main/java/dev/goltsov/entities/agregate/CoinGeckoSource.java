package dev.goltsov.entities.agregate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.goltsov.entities.request.ApiForRequest;
import dev.goltsov.entities.request.CoinGeckoApi;
import dev.goltsov.entities.response.CoinGeckoResponse;
import dev.goltsov.exception.ApiException;
import dev.goltsov.http.HttpFetcher;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CoinGeckoSource implements DataSource<CoinGeckoResponse> {
    private final HttpFetcher http;
    private final ObjectMapper mapper;
    private final CoinGeckoApi api;

    public CoinGeckoSource(HttpFetcher http, ObjectMapper mapper, CoinGeckoApi api) {
        this.http = http;
        this.mapper = mapper;
        this.api = api;
    }

    @Override
    public String name() {
        return "crypto";
    }

    @Override
    public List<AggregatedRecord> fetch(Map<String, String> additionalParams) {
        Map<String, String> params = new LinkedHashMap<>(api.getVariables());
        params.putAll(additionalParams);
        String json = http.get(api.buildUrl(params));
        return map(parse(json), params);
    }

    @Override
    public CoinGeckoResponse parse(String json) {
        try {
            return new CoinGeckoResponse(mapper.readValue(json, new TypeReference<List<CoinGeckoResponse.Coin>>() {}));
        } catch (JsonProcessingException e) {
            throw new ApiException("Bad crypto response", e);
        }
    }

    @Override
    public List<AggregatedRecord> map(CoinGeckoResponse response, Map<String, String> params) {
        if (response.coins() == null) {
            throw new ApiException("Empty crypto response");
        }
        String paramString = ApiForRequest.format(params);
        List<AggregatedRecord> result = new ArrayList<>();
        for (CoinGeckoResponse.Coin coin : response.coins()) {
            Map<String, String> values = new LinkedHashMap<>();
            values.put("coin", Objects.toString(coin.name(), ""));
            values.put("price", plain(coin.current_price()));
            values.put("marketCap", plain(coin.market_cap()));
            values.put("change24h", Objects.toString(coin.price_change_percentage_24h(), ""));
            Instant time = coin.last_updated() != null ? coin.last_updated() : Instant.now();
            result.add(new AggregatedRecord(name(), paramString, time, values));
        }
        return result;
    }

    private static String plain(BigDecimal value) {
        return value == null ? "" : value.toPlainString();
    }
}
