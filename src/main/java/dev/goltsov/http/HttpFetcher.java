package dev.goltsov.http;

import dev.goltsov.exception.ApiException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;

@Slf4j
public class HttpFetcher {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpApiClient client;

    public HttpFetcher(HttpApiClient client) {
        this.client = client;
    }

    public String get(String url) {
        long start = System.currentTimeMillis();
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.doRequest(request);
            log.info("GET {} -> {} ({} ms)", url, response.statusCode(), System.currentTimeMillis() - start);
            if (response.statusCode() != 200) {
                throw new ApiException("HTTP " + response.statusCode() + " for " + url);
            }
            return response.body();
        } catch (IllegalArgumentException e) {
            throw new ApiException("Bad URL: " + url, e);
        } catch (HttpTimeoutException e) {
            throw new ApiException("Timeout for " + url, e);
        } catch (IOException e) {
            throw new ApiException("Network error for " + url + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Request interrupted: " + url, e);
        }
    }
}
