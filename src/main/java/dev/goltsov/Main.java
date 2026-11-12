package dev.goltsov;

import dev.goltsov.exception.StorageException;
import dev.goltsov.settings.Action;
import dev.goltsov.settings.Settings;
import dev.goltsov.settings.SettingsParser;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class Main {
    public static void main(String[] args) {
        Settings settings;
        try {
            settings = new SettingsParser().getSettings(args);
        } catch (IllegalArgumentException e) {
            log.error("Invalid arguments: {}", e.getMessage());
            System.exit(1);
            return;
        }

        log.info("Started: action={}, fileType={}, file={}",
                settings.getAction(), settings.getFileType(), settings.getOutputFile());
        AppContext appContext = new AppContext(settings);
        int code = run(appContext);
        log.info("Finished with code {}", code);
        if (code != 0) {
            System.exit(code);
        }
    }

    private static int run(AppContext appContext) {
        try {
            Aggregator aggregator = appContext.getAggregator();
            Settings settings = appContext.getSettings();
            ConsolePrinter printer = new ConsolePrinter(System.out);

            if (settings.getAction() == Action.POLL) {
                PollResult result = aggregator.poll(Arrays.asList(settings.getSources()), settings.getWriteType());
                printer.printPollResult(result, settings.getOutputFile());
                return result.saved() == 0 && !result.errors().isEmpty() ? 1 : 0;
            }

            printer.printRecords(aggregator.read(Arrays.asList(settings.getShowSources())));
            return 0;
        } catch (IllegalArgumentException | StorageException e) {
            log.error(e.getMessage());
            return 1;
        } catch (RuntimeException e) {
            log.error("Unexpected error", e);
            return 1;
        }
    }
}
