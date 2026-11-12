package dev.goltsov;

import dev.goltsov.entities.agregate.AggregatedRecord;

import java.io.PrintStream;
import java.util.List;

public class ConsolePrinter {
    private final PrintStream out;

    public ConsolePrinter(PrintStream out) {
        this.out = out;
    }

    public void printRecords(List<AggregatedRecord> records) {
        if (records.isEmpty()) {
            out.println("No records");
            return;
        }
        for (AggregatedRecord r : records) {
            out.printf("%s | %s | %s%n", r.timestamp(), r.source(), r.params());
            r.values().forEach((k, v) -> out.printf("    %s = %s%n", k, v));
        }
        out.println("Total: " + records.size());
    }

    public void printPollResult(PollResult result, String file) {
        out.println("Saved records: " + result.saved() + " (file: " + file + ")");
        result.errors().forEach(e -> out.println("Error: " + e));
    }
}
