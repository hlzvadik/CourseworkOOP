package dev.goltsov.entities.request;

public class CoinGeckoApi extends ApiForRequest {
    public CoinGeckoApi() {
        super("api.coingecko.com/api/v3/coins/markets");
        param("vs_currency", "usd");
        param("ids", "bitcoin,ethereum");
    }
}
