package util;

import fighters.FighterRoster;
import fighters.FighterStats;

import java.io.*;
import java.util.*;

public class LeaderboardManager {
    private static final String FILE_NAME = "leaderboards.txt";
    private static final String ARCADE_FILE_NAME = "arcade_leaderboards.txt";
    private static final Map<String, Integer> wins = new LinkedHashMap<>();
    private static final Map<String, Integer> arcadeBest = new LinkedHashMap<>();

    static {
        for (FighterStats stats : FighterRoster.getRoster()) {
            wins.put(stats.name, 0);
            arcadeBest.put(stats.name, 0);
        }
        load();
        loadArcade();
    }

    public static synchronized void recordWin(String fighterName) {
        wins.put(fighterName, wins.getOrDefault(fighterName, 0) + 1);
        save();
    }


    public static synchronized void recordArcadeRun(String fighterName, int floors) {
        arcadeBest.put(fighterName, Math.max(arcadeBest.getOrDefault(fighterName, 0), floors));
        saveArcade();
    }

    public static synchronized String getArcadeLeaderboardText() {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(arcadeBest.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        StringBuilder sb = new StringBuilder();
        sb.append("ARCADE SURVIVAL BEST FLOORS\n");
        sb.append("=================================\n");
        sb.append(String.format("%-4s %-20s %s\n", "#", "FIGHTER", "BEST"));
        sb.append("---------------------------------\n");

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            sb.append(String.format("%-4d %-20s %d\n", rank++, entry.getKey(), entry.getValue()));
        }

        sb.append("\nArcade streak data is saved locally in ").append(ARCADE_FILE_NAME).append('.');
        return sb.toString();
    }

    public static synchronized String getLeaderboardText() {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(wins.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        StringBuilder sb = new StringBuilder();
        sb.append("NIGHTFALL TACTICS LEADERBOARDS\n");
        sb.append("=================================\n");
        sb.append(String.format("%-4s %-20s %s\n", "#", "FIGHTER", "WINS"));
        sb.append("---------------------------------\n");

        int rank = 1;
        for (Map.Entry<String, Integer> entry : sorted) {
            sb.append(String.format("%-4d %-20s %d\n", rank++, entry.getKey(), entry.getValue()));
        }

        sb.append("\nLeaderboard data is saved locally in ").append(FILE_NAME).append('.');
        return sb.toString();
    }

    private static void load() {
        File file = new File(FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    try {
                        wins.put(parts[0], Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Map.Entry<String, Integer> entry : wins.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (IOException ignored) {
        }
    }


    private static void loadArcade() {
        File file = new File(ARCADE_FILE_NAME);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    try {
                        arcadeBest.put(parts[0], Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    private static void saveArcade() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ARCADE_FILE_NAME))) {
            for (Map.Entry<String, Integer> entry : arcadeBest.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (IOException ignored) {
        }
    }

}
