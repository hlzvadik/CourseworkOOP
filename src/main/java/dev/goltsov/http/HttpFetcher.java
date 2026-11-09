package dev.goltsov.http;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

public class HttpFetcher {
    public String get(String uri) {
        try {
            return new HttpApiClient().doRequest(HttpRequest.newBuilder().uri(new URI(uri)).GET().build()).body();
        } catch (IOException | InterruptedException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
