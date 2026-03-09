package ui;

import util.SettingsManager;
import util.SoundPlayer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.Map;

public class SettingsDialog extends JDialog {
    private final JCheckBox audioEnabled = new JCheckBox("Enable Audio");
    private final JSlider masterSlider = new JSlider(0, 100);
    private final JSlider musicSlider = new JSlider(0, 100);
    private final JSlider sfxSlider = new JSlider(0, 100);
    private final Map<String, JButton> keyButtons = new LinkedHashMap<>();

    public SettingsDialog(JFrame owner) {
        super(owner, "Settings", true);
        setSize(980, 680);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(14, 14));
        getContentPane().setBackground(new Color(8, 12, 20));

        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(new Color(255, 214, 120));
        title.setBorder(BorderFactory.createEmptyBorder(18, 8, 0, 8));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Arial", Font.BOLD, 15));
        tabs.addTab("Audio", buildAudioTab());
        tabs.addTab("Keybinds", buildKeybindsTab());
        add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        bottom.setOpaque(false);
        JButton defaults = createButton("Restore Defaults");
        JButton close = createButton("Save & Close");
        defaults.addActionListener(e -> restoreDefaults());
        close.addActionListener(e -> {
            applyChanges();
            dispose();
        });
        bottom.add(defaults);
        bottom.add(close);
        add(bottom, BorderLayout.SOUTH);

        loadValues();
    }

    private JPanel buildAudioTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(10, 16, 28));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridx = 0;
        gc.gridy = 0;

        audioEnabled.setOpaque(false);
        audioEnabled.setForeground(Color.WHITE);
        audioEnabled.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(audioEnabled, gc);

        gc.gridy++;
        panel.add(makeSliderRow("Master Volume", masterSlider, "Controls all sound effects and menu feedback."), gc);
        gc.gridy++;
        panel.add(makeSliderRow("Music Volume", musicSlider, "Prepared for future background music support."), gc);
        gc.gridy++;
        panel.add(makeSliderRow("SFX Volume", sfxSlider, "Controls hit, block, knockback, and UI audio cues."), gc);

        JTextArea note = new JTextArea(
                "Audio Notes\n" +
                "• The current build uses sound effects and fallback beeps.\n" +
                "• Music volume is already saved so the next music update can use it immediately.\n" +
                "• Disable audio for quiet testing or classroom demos."
        );
        note.setOpaque(false);
        note.setEditable(false);
        note.setForeground(new Color(220, 225, 235));
        note.setFont(new Font("Arial", Font.PLAIN, 15));
        note.setBorder(BorderFactory.createEmptyBorder(16, 4, 0, 4));
        gc.gridy++;
        panel.add(note, gc);
        return panel;
    }

    private JPanel makeSliderRow(String title, JSlider slider, String helper) {
        JPanel row = new JPanel(new BorderLayout(8, 8));
        row.setOpaque(false);
        JLabel label = new JLabel(title);
        label.setForeground(new Color(255, 214, 120));
        label.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel hint = new JLabel(helper);
        hint.setForeground(new Color(215, 220, 230));
        hint.setFont(new Font("Arial", Font.PLAIN, 13));
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);
        top.add(label);
        top.add(hint);
        slider.setOpaque(false);
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        row.add(top, BorderLayout.NORTH);
        row.add(slider, BorderLayout.CENTER);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 214, 120, 90), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return row;
    }

    private JScrollPane buildKeybindsTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(10, 16, 28));
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.gridy = 0;

        JLabel help = new JLabel("Click a keybind button, then press a new key.");
        help.setForeground(new Color(220, 225, 235));
        help.setFont(new Font("Arial", Font.PLAIN, 16));
        gc.gridx = 0;
        gc.gridwidth = 3;
        panel.add(help, gc);
        gc.gridwidth = 1;

        for (Map.Entry<String, Integer> entry : SettingsManager.getAllKeyBindings().entrySet()) {
            gc.gridy++;
            gc.gridx = 0;
            JLabel label = new JLabel(pretty(entry.getKey()));
            label.setForeground(Color.WHITE);
            label.setFont(new Font("Arial", Font.BOLD, 15));
            panel.add(label, gc);

            gc.gridx = 1;
            JButton button = createButton(KeyEvent.getKeyText(entry.getValue()));
            button.addActionListener(e -> beginRebind(entry.getKey(), button));
            keyButtons.put(entry.getKey(), button);
            panel.add(button, gc);

            gc.gridx = 2;
            JLabel side = new JLabel(sideHint(entry.getKey()));
            side.setForeground(new Color(255, 214, 120));
            side.setFont(new Font("Arial", Font.PLAIN, 13));
            panel.add(side, gc);
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        return scroll;
    }

    private void beginRebind(String action, JButton button) {
        button.setText("Press a key...");
        KeyAdapter adapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                SettingsManager.setKeyCode(action, e.getKeyCode());
                button.setText(KeyEvent.getKeyText(e.getKeyCode()));
                removeKeyListener(this);
                applyChanges();
            }
        };
        addKeyListener(adapter);
        setFocusable(true);
        requestFocusInWindow();
    }

    private void loadValues() {
        audioEnabled.setSelected(SettingsManager.isAudioEnabled());
        masterSlider.setValue(SettingsManager.getMasterVolume());
        musicSlider.setValue(SettingsManager.getMusicVolume());
        sfxSlider.setValue(SettingsManager.getSfxVolume());
        for (Map.Entry<String, JButton> entry : keyButtons.entrySet()) {
            entry.getValue().setText(SettingsManager.getKeyText(entry.getKey()));
        }
    }

    private void applyChanges() {
        SettingsManager.setAudioEnabled(audioEnabled.isSelected());
        SettingsManager.setMasterVolume(masterSlider.getValue());
        SettingsManager.setMusicVolume(musicSlider.getValue());
        SettingsManager.setSfxVolume(sfxSlider.getValue());
        SoundPlayer.reloadSettings();
    }

    private void restoreDefaults() {
        SettingsManager.restoreDefaults();
        loadValues();
        SoundPlayer.reloadSettings();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setBackground(new Color(245, 190, 70));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        return button;
    }

    private String pretty(String action) {
        return action.replace('_', ' ');
    }

    private String sideHint(String action) {
        if (action.startsWith("P1_")) return "Player 1";
        if (action.startsWith("P2_")) return "Player 2";
        return "Global";
    }
}
