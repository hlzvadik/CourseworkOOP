package dev.goltsov.entities.request;

public class CoinGeckoApi extends ApiForRequest {
    public CoinGeckoApi() {
        domain = "api.coingecko.com/api/v3/coins/markets";
        variables.put("vs_currency", "usd");
    }
}
