package dev.goltsov.entities.agregate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.goltsov.entities.request.ApiForRequest;
import dev.goltsov.entities.request.CoinGeckoApi;
import dev.goltsov.entities.response.CoinGeckoResponse;
import dev.goltsov.exception.ApiException;
import dev.goltsov.http.HttpFetcher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

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
    public List<AggregatedRecord> fetch(Map<String, String> overrides) throws URISyntaxException, IOException, InterruptedException {
        Map<String, String> params = new LinkedHashMap<>(api.getVariables());
        params.putAll(overrides);
        String json = http.get(api.buildUrl(params));
        return map(parse(json), params);
    }

    @Override
    public CoinGeckoResponse parse(String json) {
        try {
            return mapper.readValue(json, new TypeReference<CoinGeckoResponse>() {});
        } catch (JsonProcessingException e) {
            throw new ApiException("Bad crypto response", e);
        }
    }

    @Override
    public List<AggregatedRecord> map(CoinGeckoResponse coins, Map<String, String> params) {
        String paramString = ApiForRequest.format(params);
        List<AggregatedRecord> result = new ArrayList<>();
        for (CoinGeckoResponse.Coin coin : coins.coins()) {
            Map<String, String> values = new LinkedHashMap<>();
            values.put("coin", Objects.toString(coin.name(), ""));
            values.put("price", Objects.toString(coin.current_price(), ""));
            values.put("marketCap", Objects.toString(coin.market_cap(), ""));
            values.put("change24h", Objects.toString(coin.price_change_percentage_24h(), ""));
            result.add(new AggregatedRecord(name(), paramString, coin.last_updated(), values));
        }
        return result;
    }
}
