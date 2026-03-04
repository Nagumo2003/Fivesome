package models;

import javax.swing.*;
import java.awt.*;
import java.util.StringTokenizer;
import models.*;

public class NightfallTactics extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainContainer = new JPanel(cardLayout);
    private Hero player, enemy;
    private JTextArea log;
    private String difficulty = "NORMAL";

    public NightfallTactics() {
        setTitle("NIGHTFALL TACTICS: ULTIMATE");
        setSize(850, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Add Screens
        mainContainer.add(createOpeningScreen(), "START");
        mainContainer.add(createSettingsMenu(), "SETTINGS");
        mainContainer.add(createBattleArena(), "BATTLE");

        add(mainContainer);
        cardLayout.show(mainContainer, "START");
        setVisible(true);
    }

    private JPanel createOpeningScreen() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(10, 10, 25));
        
        JLabel title = new JLabel("NIGHTFALL TACTICS");
        title.setFont(new Font("Impact", Font.PLAIN, 60));
        title.setForeground(Color.CYAN);

        JButton startBtn = new JButton("START MISSION");
        startBtn.addActionListener(e -> {
            player = new Hero("Christopher", 1800, 800); // Stats from notes
            enemy = new Hero("AI Assassin", 3000, 600);
            cardLayout.show(mainContainer, "BATTLE");
        });

        JButton setBtn = new JButton("SETTINGS");
        setBtn.addActionListener(e -> cardLayout.show(mainContainer, "SETTINGS"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(title, gbc);
        panel.add(startBtn, gbc);
        panel.add(setBtn, gbc);
        return panel;
    }

    private JPanel createSettingsMenu() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea console = new JTextArea("Type: 'set diff hard' or 'set diff easy'");
        JTextField input = new JTextField();

        input.addActionListener(e -> {
            // StringTokenizer usage
            StringTokenizer st = new StringTokenizer(input.getText());
            try {
                if (st.nextToken().equalsIgnoreCase("set")) {
                    String cmd = st.nextToken();
                    if (cmd.equalsIgnoreCase("diff")) {
                        difficulty = st.nextToken().toUpperCase();
                        console.append("\nDifficulty set to: " + difficulty);
                    }
                }
            } catch (Exception ex) {
                console.append("\nError: Use 'set diff [value]'");
            }
            input.setText("");
        });

        JButton back = new JButton("BACK");
        back.addActionListener(e -> cardLayout.show(mainContainer, "START"));

        panel.add(new JScrollPane(console), BorderLayout.CENTER);
        panel.add(input, BorderLayout.SOUTH);
        panel.add(back, BorderLayout.NORTH);
        return panel;
    }

    private JPanel createBattleArena() {
        JPanel arena = new JPanel(new BorderLayout());
        log = new JTextArea();
        log.setBackground(Color.BLACK);
        log.setForeground(Color.GREEN);
        
        JButton skillBtn = new JButton("USE SKILL 3 (ULTIMATE)");
        skillBtn.addActionListener(e -> {
            // Try-Catch for Battle Logic
            try {
                log.append("> " + player.useSkill(3, enemy) + "\n");
                if (enemy.isAlive()) {
                    log.append("> AI counter-attacks!\n");
                    log.append("> " + enemy.useSkill(2, player) + "\n");
                } else {
                    JOptionPane.showMessageDialog(this, "Victory!");
                    cardLayout.show(mainContainer, "START");
                }
            } catch (Exception ex) {
                log.append("> SYSTEM ERROR: " + ex.getMessage() + "\n");
            }
        });

        arena.add(new JScrollPane(log), BorderLayout.CENTER);
        arena.add(skillBtn, BorderLayout.SOUTH);
        return arena;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(NightfallTactics::new);
    }
}