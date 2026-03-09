package ui;

import util.AssetLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class OpeningScreen extends JFrame {
    public OpeningScreen() {
        setTitle("Nightfall Tactics");
        setSize(Math.max(1280, Toolkit.getDefaultToolkit().getScreenSize().width - 120), Math.max(720, Toolkit.getDefaultToolkit().getScreenSize().height - 120));
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setContentPane(new OpeningPanel(this));
    }

    private static class OpeningPanel extends JPanel {
        private final JFrame owner;
        private final BufferedImage background = AssetLoader.loadImage("assets/ui/opening_bg.png", 1280, 720);

        OpeningPanel(JFrame owner) {
            this.owner = owner;
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

            JPanel top = new JPanel(new GridLayout(2, 1));
            top.setOpaque(false);

            JLabel title = new JLabel("NIGHTFALL TACTICS", SwingConstants.CENTER);
            title.setForeground(Color.WHITE);
            title.setFont(new Font("Arial", Font.BOLD, 42));

            JLabel subtitle = new JLabel("Fantasy Arena of Corrupt Power", SwingConstants.CENTER);
            subtitle.setForeground(new Color(255, 206, 104));
            subtitle.setFont(new Font("Arial", Font.BOLD, 22));

            top.add(title);
            top.add(subtitle);
            add(top, BorderLayout.NORTH);

            JTextArea story = new JTextArea(
                    "Enter a shadowed battlefield where power, greed, and deception rule the night.\n" +
                    "Choose your fighter and battle through a fantasy world inspired by corrupt political empires.\n\n" +
                    "Features:\n" +
                    "• Arcade Mode\n" +
                    "• Player vs Player\n" +
                    "• Leaderboards\n" +
                    "• Special, stun, and knockback combo tools\n" +
                    "• Nightfall-themed arenas and fighter portraits"
            );
            story.setEditable(false);
            story.setOpaque(false);
            story.setForeground(new Color(240, 240, 240));
            story.setFont(new Font("Monospaced", Font.BOLD, 17));
            story.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
            add(story, BorderLayout.CENTER);

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 12));
            bottom.setOpaque(false);
            JButton start = createButton("PRESS START");
            JButton exit = createButton("EXIT");

            start.addActionListener(e -> {
                SwingUtilities.invokeLater(() -> {
                    MainMenuFrame menu = new MainMenuFrame();
                    menu.setVisible(true);
                });
                owner.dispose();
            });
            exit.addActionListener(e -> System.exit(0));

            bottom.add(start);
            bottom.add(exit);
            add(bottom, BorderLayout.SOUTH);
        }

        private JButton createButton(String text) {
            JButton button = new JButton(text);
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.BOLD, 18));
            button.setBackground(new Color(250, 180, 60));
            button.setForeground(Color.BLACK);
            button.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            return button;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.drawImage(background, 0, 0, getWidth(), getHeight(), null);
            g2.setColor(new Color(8, 10, 18, 145));
            g2.fillRoundRect(30, 30, getWidth() - 60, getHeight() - 60, 30, 30);
            g2.setColor(new Color(255, 200, 90, 160));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(30, 30, getWidth() - 60, getHeight() - 60, 30, 30);
            g2.dispose();
        }
    }
}
