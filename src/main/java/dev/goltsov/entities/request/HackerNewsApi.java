package dev.goltsov.entities.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HackerNewsApi extends ApiForRequest {
    public HackerNewsApi() {
        domain = "hn.algolia.com/api/v1/search";
        variables.put("query", "java");
    }
}
