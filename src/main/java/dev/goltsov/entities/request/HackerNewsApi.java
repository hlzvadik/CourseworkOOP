package dev.goltsov.entities.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HackerNewsApi extends ApiForRequest {
    public HackerNewsApi() {
        super("hn.algolia.com/api/v1/search");
        param("query", "java");
        param("tags", "story");
        param("hitsPerPage", "5");
    }
}
