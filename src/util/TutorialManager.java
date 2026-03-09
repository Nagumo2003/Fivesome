package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TutorialManager {
    private static final Path DATA_DIR = Paths.get("game_data");
    private static final Path FIRST_TIME_FLAG = DATA_DIR.resolve("tutorial_seen.flag");

    private TutorialManager() {}

    public static boolean isFirstTimeUser() {
        return !Files.exists(FIRST_TIME_FLAG);
    }

    public static void markTutorialSeen() {
        try {
            Files.createDirectories(DATA_DIR);
            if (!Files.exists(FIRST_TIME_FLAG)) {
                Files.writeString(FIRST_TIME_FLAG, "Nightfall Tactics tutorial opened\n");
            }
        } catch (IOException ignored) {
        }
    }
}
