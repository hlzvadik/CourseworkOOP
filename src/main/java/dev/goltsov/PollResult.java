package dev.goltsov;

import java.util.List;

public record PollResult(int saved, List<String> errors) {
}
