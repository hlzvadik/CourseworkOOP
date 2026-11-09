package dev.goltsov.http;

import dev.goltsov.entities.request.ApiForRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.Map;

public class HttpRequestApiFactory {
    public HttpRequest createHttpRequest(ApiForRequest apiForRequest) throws URISyntaxException {
        StringBuilder stringOfUri = new StringBuilder();
        stringOfUri.append("https://").append(apiForRequest.getDomain()).append("?");
        for (Map.Entry<String, String> entry: apiForRequest.getVariables().entrySet()) {
            stringOfUri.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        if (!apiForRequest.getVariables().isEmpty()) {
            stringOfUri.deleteCharAt(stringOfUri.length() - 1);
        }
        return HttpRequest.newBuilder().uri(new URI(stringOfUri.toString())).GET().build();
    }
}
