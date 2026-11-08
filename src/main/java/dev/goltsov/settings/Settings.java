package dev.goltsov.settings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class Settings {
    private Action action = Action.POLL;
    private String[] sources;
    private String[] showSources = new String[0];
    private FileType fileType;
    private WriteType writeType;
}