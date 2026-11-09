package dev.goltsov.entities.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public abstract class ApiForRequest {
    protected String domain;
    protected Map<String, String> variables;
}
