package util;

import java.awt.event.KeyEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class SettingsManager {
    private static final String FILE_NAME = "settings.properties";
    private static final Properties props = new Properties();
    private static final LinkedHashMap<String, Integer> defaults = new LinkedHashMap<>();

    static {
        defaults.put("P1_LEFT", KeyEvent.VK_A);
        defaults.put("P1_RIGHT", KeyEvent.VK_D);
        defaults.put("P1_JUMP", KeyEvent.VK_W);
        defaults.put("P1_CROUCH", KeyEvent.VK_S);
        defaults.put("P1_BLOCK", KeyEvent.VK_B);
        defaults.put("P1_LIGHT", KeyEvent.VK_F);
        defaults.put("P1_HEAVY", KeyEvent.VK_G);
        defaults.put("P1_KICK", KeyEvent.VK_V);
        defaults.put("P1_SPECIAL", KeyEvent.VK_R);
        defaults.put("P1_ULT", KeyEvent.VK_T);

        defaults.put("P2_LEFT", KeyEvent.VK_LEFT);
        defaults.put("P2_RIGHT", KeyEvent.VK_RIGHT);
        defaults.put("P2_JUMP", KeyEvent.VK_UP);
        defaults.put("P2_CROUCH", KeyEvent.VK_DOWN);
        defaults.put("P2_BLOCK", KeyEvent.VK_M);
        defaults.put("P2_LIGHT", KeyEvent.VK_J);
        defaults.put("P2_HEAVY", KeyEvent.VK_K);
        defaults.put("P2_KICK", KeyEvent.VK_N);
        defaults.put("P2_SPECIAL", KeyEvent.VK_O);
        defaults.put("P2_ULT", KeyEvent.VK_P);

        defaults.put("PAUSE", KeyEvent.VK_SPACE);
        defaults.put("BACK", KeyEvent.VK_ESCAPE);

        load();
    }

    public static void load() {
        try (FileInputStream in = new FileInputStream(FILE_NAME)) {
            props.load(in);
        } catch (IOException ignored) {
        }
        for (Map.Entry<String, Integer> entry : defaults.entrySet()) {
            props.putIfAbsent(entry.getKey(), String.valueOf(entry.getValue()));
        }
        props.putIfAbsent("MASTER_VOLUME", "85");
        props.putIfAbsent("MUSIC_VOLUME", "55");
        props.putIfAbsent("SFX_VOLUME", "90");
        props.putIfAbsent("AUDIO_ENABLED", "true");
        save();
    }

    public static void save() {
        try (FileOutputStream out = new FileOutputStream(FILE_NAME)) {
            props.store(out, "Nightfall Tactics Settings");
        } catch (IOException ignored) {
        }
    }

    public static Map<String, Integer> getAllKeyBindings() {
        LinkedHashMap<String, Integer> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : defaults.entrySet()) {
            copy.put(entry.getKey(), getKeyCode(entry.getKey()));
        }
        return copy;
    }

    public static int getKeyCode(String action) {
        return Integer.parseInt(props.getProperty(action, String.valueOf(defaults.getOrDefault(action, KeyEvent.VK_UNDEFINED))));
    }

    public static String getKeyText(String action) {
        return KeyEvent.getKeyText(getKeyCode(action));
    }

    public static void setKeyCode(String action, int keyCode) {
        props.setProperty(action, String.valueOf(keyCode));
        save();
    }

    public static int getMasterVolume() {
        return Integer.parseInt(props.getProperty("MASTER_VOLUME", "85"));
    }

    public static void setMasterVolume(int value) {
        props.setProperty("MASTER_VOLUME", String.valueOf(clamp(value)));
        save();
    }

    public static int getMusicVolume() {
        return Integer.parseInt(props.getProperty("MUSIC_VOLUME", "55"));
    }

    public static void setMusicVolume(int value) {
        props.setProperty("MUSIC_VOLUME", String.valueOf(clamp(value)));
        save();
    }

    public static int getSfxVolume() {
        return Integer.parseInt(props.getProperty("SFX_VOLUME", "90"));
    }

    public static void setSfxVolume(int value) {
        props.setProperty("SFX_VOLUME", String.valueOf(clamp(value)));
        save();
    }

    public static boolean isAudioEnabled() {
        return Boolean.parseBoolean(props.getProperty("AUDIO_ENABLED", "true"));
    }

    public static void setAudioEnabled(boolean enabled) {
        props.setProperty("AUDIO_ENABLED", String.valueOf(enabled));
        save();
    }

    public static void restoreDefaults() {
        for (Map.Entry<String, Integer> entry : defaults.entrySet()) {
            props.setProperty(entry.getKey(), String.valueOf(entry.getValue()));
        }
        props.setProperty("MASTER_VOLUME", "85");
        props.setProperty("MUSIC_VOLUME", "55");
        props.setProperty("SFX_VOLUME", "90");
        props.setProperty("AUDIO_ENABLED", "true");
        save();
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
