package util;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;

public class SoundPlayer {
    private static boolean enabled = true;
    private static int masterVolume = 85;
    private static int sfxVolume = 90;

    static {
        reloadSettings();
    }

    public static void reloadSettings() {
        enabled = SettingsManager.isAudioEnabled();
        masterVolume = SettingsManager.getMasterVolume();
        sfxVolume = SettingsManager.getSfxVolume();
    }

    public static void play(String path) {
        if (!enabled) return;
        try {
            File file = new File(path);
            if (!file.exists()) {
                beep();
                return;
            }
            AudioInputStream audio = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audio);
            applyVolume(clip);
            clip.start();
        } catch (Exception ex) {
            beep();
        }
    }

    private static void applyVolume(Clip clip) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            double fraction = Math.max(0.0001, (masterVolume / 100.0) * (sfxVolume / 100.0));
            float dB = (float) (20.0 * Math.log10(fraction));
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB)));
        } catch (Exception ignored) {
        }
    }

    private static void beep() {
        if (enabled && masterVolume > 0 && sfxVolume > 0) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public static void setEnabled(boolean value) {
        enabled = value;
    }
}
