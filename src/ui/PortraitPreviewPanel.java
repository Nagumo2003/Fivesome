package ui;

import fighters.FighterStats;
import util.AssetLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class PortraitPreviewPanel extends JPanel {
    private FighterStats fighter;
    private boolean hovered;
    private int frameIndex;
    private double turnAngle;
    private double hoverScale;
    private final Timer hoverTimer;

    public PortraitPreviewPanel() {
        setPreferredSize(new Dimension(260, 340));
        setOpaque(false);

        hoverTimer = new Timer(95, e -> {
            frameIndex = (frameIndex + 1) % 5;
            turnAngle += 0.20;
            hoverScale = 0.98 + Math.abs(Math.sin(turnAngle)) * 0.05;
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovered = true;
                if (!hoverTimer.isRunning()) {
                    hoverTimer.start();
                }
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovered = false;
                hoverTimer.stop();
                frameIndex = 0;
                turnAngle = 0;
                hoverScale = 1.0;
                repaint();
            }
        });
    }

    public void setFighter(FighterStats fighter) {
        this.fighter = fighter;
        this.frameIndex = 0;
        this.turnAngle = 0;
        this.hoverScale = 1.0;
        repaint();
    }

    private String safe(String name) {
        return name.toLowerCase().replace(" ", "_");
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        int w = getWidth();
        int h = getHeight();

        GradientPaint gp = new GradientPaint(0, 0, new Color(20, 26, 44, 235), 0, h, new Color(7, 10, 19, 235));
        g2.setPaint(gp);
        g2.fillRoundRect(0, 0, w - 1, h - 1, 24, 24);
        g2.setColor(new Color(245, 190, 70));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 24, 24);

        if (fighter != null) {
            drawHeader(g2, w);
            if (hovered) {
                draw360Preview(g2, w, h);
            } else {
                drawPortraitCard(g2, w, h);
            }
            drawStats(g2, w, h);
        }
        g2.dispose();
    }

    private void drawHeader(Graphics2D g2, int w) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 19));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(fighter.name, (w - fm.stringWidth(fighter.name)) / 2, 28);

        g2.setColor(new Color(255, 210, 115));
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String text = hovered ? "360 PREVIEW ACTIVE" : "Hover for turntable preview";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(text, (w - fm2.stringWidth(text)) / 2, 46);
    }

    private void drawPortraitCard(Graphics2D g2, int w, int h) {
        BufferedImage portrait = AssetLoader.loadImage("assets/portraits/" + safe(fighter.name) + "_portrait.png", 156, 156);
        int px = (w - 156) / 2;
        int py = 58;
        g2.setColor(new Color(255, 210, 120, 40));
        g2.fillOval(px - 22, py - 18, 200, 200);
        g2.drawImage(portrait, px, py, null);
        g2.setColor(new Color(255, 210, 120, 70));
        g2.drawRoundRect(px - 8, py - 8, 172, 172, 20, 20);

        g2.setColor(new Color(220, 225, 240));
        g2.setFont(new Font("Arial", Font.PLAIN, 13));
        drawCentered(g2, "Select this fighter, then hover to inspect the animated turntable.", w, h - 130);
    }

    private void draw360Preview(Graphics2D g2, int w, int h) {
        String key = safe(fighter.name);
        BufferedImage sheet = AssetLoader.loadImage("assets/sheets/" + key + "_sheet.png", 320, 120);
        int frameCount = 5;
        int fw = Math.max(1, sheet.getWidth() / frameCount);
        int fh = sheet.getHeight();
        int index = Math.min(frameIndex, frameCount - 1);
        BufferedImage frame = sheet.getSubimage(index * fw, 0, fw, fh);

        int previewTop = 58;
        int previewBottom = h - 136;
        int previewHeight = Math.max(132, previewBottom - previewTop);
        int previewWidth = w - 34;
        int previewX = 17;

        g2.setColor(new Color(255, 215, 120, 24));
        g2.fillRoundRect(previewX, previewTop, previewWidth, previewHeight, 20, 20);
        g2.setColor(new Color(255, 215, 120, 70));
        g2.drawRoundRect(previewX, previewTop, previewWidth, previewHeight, 20, 20);

        int pedestalY = previewTop + previewHeight - 22;
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillOval(w / 2 - 72, pedestalY, 144, 20);
        g2.setColor(new Color(255, 210, 120, 110));
        g2.drawOval(w / 2 - 72, pedestalY, 144, 20);

        double bob = Math.sin(turnAngle * 1.35) * 2.5;
        double widthPulse = hoverScale;
        double depthTilt = Math.sin(turnAngle) * 0.02;

        BufferedImage padded = new BufferedImage(frame.getWidth() + 120, frame.getHeight() + 120, BufferedImage.TYPE_INT_ARGB);
        Graphics2D pg = padded.createGraphics();
        pg.drawImage(frame, 60, 40, null);
        pg.dispose();

        int safeW = previewWidth - 30;
        int safeH = previewHeight - 36;
        double scale = Math.min(safeW / (double) padded.getWidth(), safeH / (double) padded.getHeight());
        scale *= 0.92;
        int drawW = Math.max(1, (int) Math.round(padded.getWidth() * scale * widthPulse));
        int drawH = Math.max(1, (int) Math.round(padded.getHeight() * scale));
        int centerX = w / 2;
        int centerY = previewTop + previewHeight / 2 + 6 + (int) bob;

        Shape oldClip = g2.getClip();
        g2.setClip(previewX + 8, previewTop + 8, previewWidth - 16, previewHeight - 16);
        AffineTransform old = g2.getTransform();
        g2.translate(centerX, centerY);
        g2.shear(depthTilt, 0);
        if (Math.cos(turnAngle) < 0) {
            g2.scale(-1, 1);
        }
        g2.drawImage(padded, -drawW / 2, -drawH / 2, drawW, drawH, null);
        g2.setTransform(old);
        g2.setClip(oldClip);

        g2.setColor(new Color(255, 210, 120));
        g2.setFont(new Font("Arial", Font.BOLD, 13));
        drawCentered(g2, "Safer turntable preview with padded margins and cleaner fit", w, h - 116);
        g2.setColor(new Color(215, 220, 235));
        g2.setFont(new Font("Arial", Font.PLAIN, 12));
        drawCentered(g2, "Move your mouse away to return to the portrait card.", w, h - 98);
    }

    private void drawStats(Graphics2D g2, int w, int h) {
        int cardY = h - 96;
        g2.setColor(new Color(16, 20, 34, 235));
        g2.fillRoundRect(12, cardY, w - 24, 84, 18, 18);
        g2.setColor(new Color(245, 190, 70, 180));
        g2.drawRoundRect(12, cardY, w - 24, 84, 18, 18);

        int power = (fighter.lightDamage + fighter.heavyDamage + fighter.ultimateDamage / 2) / 8;
        int speed = (int) Math.round(fighter.speed * 1.2);
        int control = Math.max(1, fighter.maxMana / 20);
        String difficulty = speed >= 6 ? "Hard" : (power >= 6 ? "Medium" : "Easy");

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.drawString("HP: " + fighter.maxHealth, 22, cardY + 18);
        g2.drawString("SPD: " + fighter.speed, 22, cardY + 34);
        g2.drawString("MP: " + fighter.maxMana, w / 2 + 6, cardY + 18);
        g2.drawString("DIFF: " + difficulty, w / 2 + 6, cardY + 34);
        g2.drawString("POW: " + bars(power), 22, cardY + 50);
        g2.drawString("CTL: " + bars(control), w / 2 + 6, cardY + 50);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(255, 220, 120));
        g2.drawString("SP: " + fighter.specialName, 22, cardY + 66);
        g2.drawString("ULT: " + fighter.ultimateName, 22, cardY + 80);
    }

    private String bars(int count) {
        int capped = Math.max(1, Math.min(6, count));
        return "■".repeat(capped);
    }

    private void drawCentered(Graphics2D g2, String text, int width, int y) {
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (width - fm.stringWidth(text)) / 2, y);
    }
}
