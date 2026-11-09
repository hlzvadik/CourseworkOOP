package dev.goltsov.entities.request;

import lombok.Getter;
import lombok.Setter;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class ApiForRequest {
    private final String domain;
    private final Map<String, String> variables = new LinkedHashMap<>();

    protected ApiForRequest(String domain) {
        this.domain = domain;
    }

    protected void param(String key, String value) {
        variables.put(key, value);
    }

    public Map<String, String> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    public String buildUrl(Map<String, String> params) {
        StringJoiner query = new StringJoiner("&");
        params.forEach((k, v) -> query.add(k + "=" + URLEncoder.encode(v, StandardCharsets.UTF_8)));
        return "https://" + domain + "?" + query;
    }

    public static String format(Map<String, String> params) {
        return params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }
}
