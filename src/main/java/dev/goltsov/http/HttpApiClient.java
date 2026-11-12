package dev.goltsov.http;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class HttpApiClient {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public HttpResponse<String> doRequest(HttpRequest httpRequest) throws IOException, InterruptedException {
        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}
