package ui;

import engine.ArcadeDifficulty;
import engine.GameMode;
import fighters.FighterRoster;
import fighters.FighterStats;
import util.AssetLoader;
import util.LeaderboardManager;
import util.TutorialManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

public class MainMenuFrame extends JFrame {
    private final JComboBox<String> p1Box = new JComboBox<>();
    private final JComboBox<String> p2Box = new JComboBox<>();
    private final PortraitPreviewPanel p1Portrait = new PortraitPreviewPanel();
    private final PortraitPreviewPanel p2Portrait = new PortraitPreviewPanel();
    private final List<FighterStats> roster = FighterRoster.getRoster();
    private final BufferedImage infoBackground = AssetLoader.loadImage("assets/ui/info_bg.png", 960, 600);

    public MainMenuFrame() {
        setTitle("Nightfall Tactics - Main Menu");
        setSize(Math.max(1280, Toolkit.getDefaultToolkit().getScreenSize().width - 120), Math.max(760, Toolkit.getDefaultToolkit().getScreenSize().height - 120));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(new BackgroundPanel());
        setLayout(new BorderLayout(12, 12));

        JPanel titlePanel = new JPanel(new GridLayout(3, 1));
        titlePanel.setOpaque(false);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(18, 12, 6, 12));
        JLabel title = new JLabel("NIGHTFALL TACTICS", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 34));
        JLabel subtitle = new JLabel("Main Menu", SwingConstants.CENTER);
        subtitle.setForeground(new Color(240, 210, 120));
        subtitle.setFont(new Font("Arial", Font.PLAIN, 18));
        JLabel hint = new JLabel("Select fighters on the right, hover a preview card for a safer turntable animation, review each fighter's difficulty and move list, then choose a mode from the left.", SwingConstants.CENTER);
        hint.setForeground(new Color(210, 210, 210));
        titlePanel.add(title);
        titlePanel.add(subtitle);
        titlePanel.add(hint);
        add(titlePanel, BorderLayout.NORTH);

        for (FighterStats stats : roster) {
            p1Box.addItem(stats.name);
            p2Box.addItem(stats.name);
        }
        p2Box.setSelectedIndex(Math.min(1, roster.size() - 1));
        p1Box.addActionListener(e -> updatePortraits());
        p2Box.addActionListener(e -> updatePortraits());

        JPanel menuPanel = new JPanel(new GridLayout(7, 1, 0, 14));
        menuPanel.setBackground(new Color(10, 14, 24, 220));
        menuPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(232, 188, 84, 180), 2),
                BorderFactory.createEmptyBorder(22, 18, 22, 18)
        ));

        JButton arcade = createButton("ARCADE MODE");
        JButton pvp = createButton("PLAYER VS PLAYER");
        JButton leaderboards = createButton("LEADERBOARDS");
        JButton about = createButton("ABOUT");
        JButton tutorial = createButton("TUTORIAL");
        JButton settings = createButton("SETTINGS");
        JButton exit = createButton("EXIT");

        arcade.addActionListener(e -> start(GameMode.ARCADE));
        pvp.addActionListener(e -> start(GameMode.PVP));
        leaderboards.addActionListener(e -> showLeaderboards());
        about.addActionListener(e -> showAbout());
        tutorial.addActionListener(e -> showTutorial(true));
        settings.addActionListener(e -> showSettings());
        exit.addActionListener(e -> System.exit(0));

        menuPanel.add(arcade);
        menuPanel.add(pvp);
        menuPanel.add(leaderboards);
        menuPanel.add(about);
        menuPanel.add(tutorial);
        menuPanel.add(settings);
        menuPanel.add(exit);

        JPanel selectors = new JPanel(new GridLayout(1, 2, 20, 20));
        selectors.setOpaque(false);
        selectors.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        selectors.add(buildSelectCard("Player 1 Fighter", p1Box, p1Portrait));
        selectors.add(buildSelectCard("Player 2 / Arcade Opponent", p2Box, p2Portrait));

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(8, 22, 18, 22));
        center.add(menuPanel, BorderLayout.WEST);
        center.add(selectors, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        updatePortraits();

        if (TutorialManager.isFirstTimeUser()) {
            SwingUtilities.invokeLater(() -> showTutorial(false));
        }
    }

    private JPanel buildSelectCard(String labelText, JComboBox<String> box, PortraitPreviewPanel preview) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(new Color(10, 14, 24, 220));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(232, 188, 84, 180), 2),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        JPanel top = new JPanel(new GridLayout(3, 1, 4, 4));
        top.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        box.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel helper = new JLabel("Hover the preview panel below for a 360 animated showcase with larger safe margins.");
        helper.setFont(new Font("Arial", Font.PLAIN, 12));
        helper.setForeground(new Color(220, 225, 240));
        top.add(label);
        top.add(box);
        top.add(helper);
        card.add(top, BorderLayout.NORTH);
        card.add(preview, BorderLayout.CENTER);
        return card;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(245, 190, 70));
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        return button;
    }

    private void updatePortraits() {
        p1Portrait.setFighter(roster.get(p1Box.getSelectedIndex()));
        p2Portrait.setFighter(roster.get(p2Box.getSelectedIndex()));
    }

    private void showLeaderboards() {
        JTextArea area = new JTextArea(LeaderboardManager.getLeaderboardText() + "\n\n" + LeaderboardManager.getArcadeLeaderboardText());
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(new Font("Monospaced", Font.BOLD, 20));
        area.setForeground(Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(25, 28, 25, 28));

        JPanel panel = createInfoPanel("LEADERBOARDS", area);
        JOptionPane.showMessageDialog(this, panel, "Leaderboards", JOptionPane.PLAIN_MESSAGE);
    }

    private void showTutorial(boolean fromMenu) {
        JTextArea area = new JTextArea(
                "WELCOME TO NIGHTFALL TACTICS\n" +
                "=============================\n\n" +
                "This tutorial helps first-time players understand the basic mechanics before entering battle.\n\n" +
                "1. MAIN GOAL\n" +
                "Win 2 rounds before your opponent in a best-of-3 match. Reduce the enemy HP to zero, or have more HP when time runs out.\n\n" +
                "2. CORE MOVEMENT\n" +
                "Player 1: A / D move, W jump, S crouch\n" +
                "Player 2: Left / Right move, Up jump, Down crouch\n\n" +
                "3. ATTACK OPTIONS\n" +
                "Use light, heavy, and kick attacks to build pressure. Every fighter now has a unique special and ultimate, with different range, knockback, stun, launch, and mana refund behavior.\n\n" +
                "4. STUN + KNOCKBACK\n" +
                "Stun freezes the enemy for a short time. Knockback pushes the enemy away, giving you spacing, wall pressure, and new combo routes. Mix both to create longer chains.\n\n" +
                "5. DEFENSE\n" +
                "Block incoming attacks to reduce risk. Good defense lets you wait for an opening and counterattack.\n\n" +
                "6. GAME MODES\n" +
                "Arcade Mode = fight the AI.\n" +
                "Player vs Player = local 2-player battle.\n" +
                "Leaderboards = see recorded winners.\n\n" +
                "7. FIRST MATCH TIP\n" +
                "Try a simple combo like light -> heavy -> special. Once you land an ultimate, continue pressure while the enemy is stunned or knocked back.\n\n" +
                "Good luck in the shadows of Nightfall Tactics."
        );
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(new Font("Monospaced", Font.BOLD, 17));
        area.setForeground(Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(25, 28, 25, 28));

        JPanel panel = createInfoPanel("TUTORIAL", area);
        JOptionPane.showMessageDialog(this, panel, fromMenu ? "Tutorial" : "Welcome Tutorial", JOptionPane.PLAIN_MESSAGE);
        TutorialManager.markTutorialSeen();
    }


    private void showSettings() {
        SettingsDialog dialog = new SettingsDialog(this);
        dialog.setVisible(true);
    }

    private void showAbout() {
        JTextArea area = new JTextArea(
                "Nightfall Tactics\n" +
                "===================\n\n" +
                "Nightfall Tactics is a fantasy fighting game prototype built in Java Swing.\n" +
                "Its world is inspired by shadowy empires, greed, political betrayal, and the fight to reclaim power from the corrupt.\n\n" +
                "Game Features:\n" +
                "• Arcade Mode against AI\n" +
                "• Player vs Player local match\n" +
                "• Best-of-3 rounds\n" +
                "• Special and ultimate attacks\n" +
                "• Stun and knockback for combo options\n" +
                "• Character portraits and themed arenas\n\n" +
                "Controls:\n" +
                "Player 1 - A/D move, W jump, S crouch, B block, F/G/V attacks, R special, T ultimate\n" +
                "Player 2 - Left/Right move, Up jump, Down crouch, M block, J/K/N attacks, O special, P ultimate"
        );
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(new Font("Monospaced", Font.BOLD, 17));
        area.setForeground(Color.WHITE);
        area.setBorder(BorderFactory.createEmptyBorder(25, 28, 25, 28));

        JPanel panel = createInfoPanel("ABOUT THE GAME", area);
        JOptionPane.showMessageDialog(this, panel, "About", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel createInfoPanel(String heading, JComponent content) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(infoBackground, 0, 0, getWidth(), getHeight(), null);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(new Color(8, 10, 18, 170));
                g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 30, 30);
                g2.setColor(new Color(245, 190, 70, 190));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 30, 30);
                g2.dispose();
            }
        };
        panel.setPreferredSize(new Dimension(820, 500));
        panel.setOpaque(false);
        JLabel label = new JLabel(heading, SwingConstants.CENTER);
        label.setForeground(new Color(255, 211, 108));
        label.setFont(new Font("Arial", Font.BOLD, 28));
        label.setBorder(BorderFactory.createEmptyBorder(18, 12, 0, 12));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(content), BorderLayout.CENTER);
        ((JScrollPane) panel.getComponent(1)).setOpaque(false);
        ((JScrollPane) panel.getComponent(1)).getViewport().setOpaque(false);
        ((JScrollPane) panel.getComponent(1)).setBorder(BorderFactory.createEmptyBorder());
        return panel;
    }

    private void start(GameMode mode) {
        FighterStats left = roster.get(p1Box.getSelectedIndex());
        FighterStats right = roster.get(p2Box.getSelectedIndex());
        ArcadeDifficulty difficulty = ArcadeDifficulty.NORMAL;
        if (mode == GameMode.ARCADE) {
            difficulty = chooseArcadeDifficulty();
            if (difficulty == null) {
                return;
            }
        }

        JFrame frame = new JFrame(mode == GameMode.PVP ? "Nightfall Tactics - Player vs Player" : "Nightfall Tactics - Arcade Mode");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setContentPane(new engine.GamePanel(mode, left, right, difficulty));
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setSize(screen);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(frame);
        }
        frame.setVisible(true);
        frame.toFront();
    }

    private ArcadeDifficulty chooseArcadeDifficulty() {
        JComboBox<ArcadeDifficulty> box = new JComboBox<>(ArcadeDifficulty.values());
        box.setSelectedItem(ArcadeDifficulty.BEGINNER);
        JTextArea notes = new JTextArea(
                "Choose an arcade run difficulty.\n\n" +
                "Beginner: extra continue, gentler enemy scaling.\n" +
                "Normal: balanced default challenge.\n" +
                "Hard: stronger AI and tighter recovery.\n" +
                "Nightmare: no continues and stronger bosses."
        );
        notes.setEditable(false);
        notes.setOpaque(false);
        notes.setForeground(Color.WHITE);
        notes.setFont(new Font("Arial", Font.PLAIN, 15));
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);
        content.add(notes, BorderLayout.NORTH);
        content.add(box, BorderLayout.SOUTH);
        JPanel panel = createInfoPanel("ARCADE DIFFICULTY", content);
        int result = JOptionPane.showConfirmDialog(this, panel, "Arcade Difficulty", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        return result == JOptionPane.OK_OPTION ? (ArcadeDifficulty) box.getSelectedItem() : null;
    }

    private class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(infoBackground, 0, 0, getWidth(), getHeight(), null);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(5, 8, 18, 120));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }
}
