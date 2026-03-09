package engine;

import ai.SimpleFighterAI;
import engine.ArcadeDifficulty;
import fighters.AttackResult;
import fighters.Fighter;
import fighters.FighterStats;
import ui.MainMenuFrame;
import ui.ResultFrame;
import util.AssetLoader;
import util.LeaderboardManager;
import util.SoundPlayer;
import util.SettingsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel {
    public static final int PANEL_W = 1920;
    public static final int PANEL_H = 1080;
    private final InputState input = new InputState();
    private final Timer timer;
    private final GameMode mode;
    private Fighter p1;
    private Fighter p2;
    private final SimpleFighterAI ai = new SimpleFighterAI();
    private final BufferedImage stage;
    private BufferedImage p1Portrait;
    private BufferedImage p2Portrait;
    private final List<HitEffect> hitEffects = new ArrayList<>();
    private final List<MoneyParticle> moneyParticles = new ArrayList<>();
    private final List<StageGlowParticle> glowParticles = new ArrayList<>();
    private long lastParticleSpawn = 0;
    private int stagePulse = 0;
    private String overlayText = "ROUND 1";
    private long overlayUntil = 0;
    private int roundNumber = 1;
    private int countdown = 60;
    private long lastSecondTick;
    private boolean roundOver = false;
    private boolean matchOver = false;
    private boolean paused = false;
    private String comboAnnouncement = "";
    private long comboAnnouncementUntil = 0;
    private String eventAnnouncement = "";
    private long eventAnnouncementUntil = 0;
    private long shakeUntil = 0;
    private int shakeStrength = 0;
    private final List<FighterStats> arcadeRoster = fighters.FighterRoster.getRoster();
    private FighterStats playerOneStats;
    private FighterStats currentOpponentStats;
    private int arcadeWins = 0;
    private int arcadeFloor = 1;
    private int arcadeDifficulty = 1;
    private boolean bossFloor = false;
    private final ArcadeDifficulty arcadeSetting;
    private int continuesRemaining;
    private String beginnerHint = "Tap Light, Kick, then Special. Blocking is always safe for beginners.";

    public GamePanel(GameMode mode, FighterStats left, FighterStats right) {
        this(mode, left, right, ArcadeDifficulty.NORMAL);
    }

    public GamePanel(GameMode mode, FighterStats left, FighterStats right, ArcadeDifficulty arcadeSetting) {
        this.mode = mode;
        this.arcadeSetting = arcadeSetting == null ? ArcadeDifficulty.NORMAL : arcadeSetting;
        this.continuesRemaining = this.arcadeSetting.continues;
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(input);

        this.playerOneStats = left;
        this.currentOpponentStats = right;
        p1 = buildFighter(left, 300, true);
        FighterStats initialOpponent = mode == GameMode.ARCADE ? createArcadeOpponentStats(right, 1, false) : right;
        p2 = buildFighter(initialOpponent, 1490, false);
        p2.setAiControlled(mode != GameMode.PVP);

        stage = AssetLoader.loadImage("assets/stages/corrupt_capitol_arena.png", PANEL_W, PANEL_H);
        p1Portrait = loadPortrait(left);
        p2Portrait = loadPortrait(right);
        if (mode == GameMode.ARCADE) {
            beginnerHint = switch (this.arcadeSetting) {
                case BEGINNER -> "Arcade tip: block first, use Special often, and your run has extra safety with more recovery.";
                case HARD -> "Hard tip: keep mana for boss floors and punish after blocks.";
                case NIGHTMARE -> "Nightmare tip: every mistake hurts. End rounds quickly and save Ultimate for bosses.";
                default -> "Arcade tip: block often, spend Special early, and save Ultimate when the enemy is low.";
            };
        }

        installKeyBindings();
        startRoundIntroSequence(mode == GameMode.ARCADE ? "ARCADE FLOOR 1 • " + this.arcadeSetting.label.toUpperCase() : "ROUND 1");
        lastSecondTick = System.currentTimeMillis();

        timer = new Timer(16, this::tick);
        timer.start();
    }

    private void installKeyBindings() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(KeyStroke.getKeyStroke(SettingsManager.getKeyCode("BACK"), 0), "leaveMatch");
        am.put("leaveMatch", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Window window = SwingUtilities.getWindowAncestor(GamePanel.this);
                if (window != null) {
                    window.dispose();
                }
                SwingUtilities.invokeLater(() -> {
                    MainMenuFrame menu = new MainMenuFrame();
                    menu.setVisible(true);
                });
            }
        });

        im.put(KeyStroke.getKeyStroke(SettingsManager.getKeyCode("PAUSE"), 0), "togglePause");
        am.put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!matchOver && System.currentTimeMillis() > overlayUntil) {
                    paused = !paused;
                }
            }
        });
    }

    private static String safe(String name) {
        return name.toLowerCase().replace(" ", "_");
    }

    private Fighter buildFighter(FighterStats stats, double spawnX, boolean facingRightDefault) {
        return new Fighter(stats, spawnX, facingRightDefault, "assets/sprites/" + safe(stats.name) + ".png");
    }

    private BufferedImage loadPortrait(FighterStats stats) {
        return AssetLoader.loadImage("assets/portraits/" + safe(stats.name) + "_portrait.png", 84, 84);
    }

    private FighterStats createArcadeOpponentStats(FighterStats base, int floor, boolean boss) {
        double healthScale = arcadeSetting.enemyHealthMultiplier + Math.min(0.40, floor * 0.025);
        double aiScale = arcadeSetting.aiMultiplier;
        if (boss) {
            healthScale += 0.28;
            aiScale += 0.18;
        }
        int hp = (int) Math.round(base.maxHealth * healthScale);
        int maxMana = (int) Math.round(base.maxMana * (boss ? 1.18 : 1.05));
        return new FighterStats(
                base.name,
                hp,
                base.speed * (boss ? 1.04 : 1.0),
                (int) Math.round(base.lightDamage * aiScale),
                (int) Math.round(base.heavyDamage * aiScale),
                (int) Math.round(base.kickDamage * aiScale),
                (int) Math.round(base.specialDamage * aiScale),
                (int) Math.round(base.ultimateDamage * aiScale),
                maxMana,
                base.specialCost,
                base.ultimateCost,
                base.specialName,
                base.ultimateName,
                base.specialTag,
                base.ultimateTag,
                base.specialRange,
                base.ultimateRange,
                boss ? base.specialKnockback + 8 : base.specialKnockback,
                boss ? base.ultimateKnockback + 12 : base.ultimateKnockback,
                base.specialLift,
                base.ultimateLift,
                boss ? base.specialStunMs + 120 : base.specialStunMs,
                boss ? base.ultimateStunMs + 150 : base.ultimateStunMs,
                Math.max(1200, base.specialCooldownMs - (boss ? 120 : 0)),
                Math.max(2600, base.ultimateCooldownMs - (boss ? 160 : 0)),
                base.specialManaRefundOnHit,
                base.ultimateManaRefundOnHit
        );
    }

    private void restorePlayerForContinue() {
        p1.resetPositionOnly(300);
        p1.heal(Math.max(p1.getMaxHealth() / 2, 1));
        p1.refillMana(Math.max(p1.getMaxMana() / 2, 1));
        p1.resetCooldowns();
        p2.resetForRound(1490);
        countdown = bossFloor ? 65 : 55;
        lastSecondTick = System.currentTimeMillis();
        roundOver = false;
        hitEffects.clear();
        comboAnnouncement = "";
        eventAnnouncement = "Continue used! Finish floor " + arcadeFloor + ".";
        eventAnnouncementUntil = System.currentTimeMillis() + 1600;
        startRoundIntroSequence("CONTINUE • FLOOR " + arcadeFloor);
        timer.start();
    }

    private void handleArcadeDefeat() {
        timer.stop();
        LeaderboardManager.recordArcadeRun(playerOneStats.name, arcadeWins);
        if (continuesRemaining > 0) {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "You were defeated on floor " + arcadeFloor + ".\nFloors cleared: " + arcadeWins + "\nContinues left: " + continuesRemaining + "\n\nUse one continue and restart this floor with 50% HP/MP?",
                    "Continue?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                continuesRemaining--;
                restorePlayerForContinue();
                return;
            }
        }
        matchOver = true;
        final int wins = arcadeWins;
        EventQueue.invokeLater(() -> new ResultFrame("Arcade run over! Floors cleared: " + wins + " • Difficulty: " + arcadeSetting.label));
    }

    private FighterStats pickNextArcadeOpponent() {
        List<FighterStats> pool = new ArrayList<>(arcadeRoster);
        pool.removeIf(s -> s.name.equals(playerOneStats.name));
        if (pool.isEmpty()) return playerOneStats;
        int index = (arcadeWins + arcadeFloor + (int)(System.currentTimeMillis() % pool.size())) % pool.size();
        return pool.get(index);
    }

    private void startNextArcadeFight() {
        arcadeFloor = arcadeWins + 1;
        bossFloor = arcadeFloor % 5 == 0;
        arcadeDifficulty = Math.max(1, Math.min(8, (int) Math.round((1 + arcadeWins / 2.0) * arcadeSetting.aiMultiplier) + (bossFloor ? 1 : 0)));
        currentOpponentStats = pickNextArcadeOpponent();
        FighterStats scaledOpponent = createArcadeOpponentStats(currentOpponentStats, arcadeFloor, bossFloor);
        p2 = buildFighter(scaledOpponent, 1490, false);
        p2.setAiControlled(true);
        p2Portrait = loadPortrait(currentOpponentStats);

        int healAmount = switch (arcadeSetting) {
            case BEGINNER -> Math.max(34, p1.getMaxHealth() / 4);
            case HARD -> Math.max(20, p1.getMaxHealth() / 7);
            case NIGHTMARE -> Math.max(12, p1.getMaxHealth() / 9);
            default -> Math.max(26, p1.getMaxHealth() / 5);
        };
        int manaAmount = switch (arcadeSetting) {
            case BEGINNER -> Math.max(26, p1.getMaxMana() / 2);
            case HARD -> Math.max(16, p1.getMaxMana() / 4);
            case NIGHTMARE -> Math.max(10, p1.getMaxMana() / 5);
            default -> Math.max(20, p1.getMaxMana() / 3);
        };
        p1.heal(healAmount);
        p1.refillMana(manaAmount);
        p1.resetCooldowns();
        p1.resetPositionOnly(300);

        p2.resetForRound(1490);
        countdown = bossFloor ? 65 : 55;
        lastSecondTick = System.currentTimeMillis();
        roundOver = false;
        hitEffects.clear();
        moneyParticles.clear();
        comboAnnouncement = "";
        eventAnnouncement = bossFloor
                ? "BOSS FLOOR! " + currentOpponentStats.name + " steps out with empowered stats."
                : "Next challenger: " + currentOpponentStats.name + "  •  AI tier " + arcadeDifficulty;
        eventAnnouncementUntil = System.currentTimeMillis() + 2200;
        startRoundIntroSequence((bossFloor ? "BOSS FLOOR " : "ARCADE FLOOR ") + arcadeFloor);
    }

    private void tick(ActionEvent e) {
        long now = System.currentTimeMillis();

        if (!paused && !matchOver) {
            if (!roundOver && now > overlayUntil) {
                processInputs(now);
                p1.update(now, PANEL_W);
                p2.update(now, PANEL_W);

                if (mode != GameMode.PVP) {
                    int aiTier = mode == GameMode.ARCADE ? arcadeDifficulty : 2;
                    AttackResult aiAtk = ai.update(p2, p1, now, aiTier);
                    applyAttack(aiAtk, p1, now);
                }

                updateEffects(now);
                updateTimer(now);
                checkRoundEnd(now);
            } else if (roundOver && now > overlayUntil) {
                if (mode == GameMode.ARCADE) {
                    if (!p1.isAlive() || countdown <= 0 && p1.getHealth() <= p2.getHealth()) {
                        handleArcadeDefeat();
                    } else {
                        arcadeWins++;
                        LeaderboardManager.recordWin(p1.getName());
                        LeaderboardManager.recordArcadeRun(playerOneStats.name, arcadeWins);
                        startNextArcadeFight();
                    }
                } else if (p1.getRoundsWon() >= 2 || p2.getRoundsWon() >= 2) {
                    matchOver = true;
                    timer.stop();
                    String winner = getWinnerName();
                    LeaderboardManager.recordWin(winner);
                    EventQueue.invokeLater(() -> new ResultFrame(winner + " WINS THE MATCH!"));
                } else {
                    roundNumber++;
                    resetRound();
                }
            }
        }
        repaint();
    }

    private void processInputs(long now) {
        p1.startMoveLeft(input.isPressed(SettingsManager.getKeyCode("P1_LEFT")));
        p1.startMoveRight(input.isPressed(SettingsManager.getKeyCode("P1_RIGHT")));
        p1.setCrouching(input.isPressed(SettingsManager.getKeyCode("P1_CROUCH")));
        p1.setBlocking(input.isPressed(SettingsManager.getKeyCode("P1_BLOCK")));
        if (input.isPressed(SettingsManager.getKeyCode("P1_JUMP"))) p1.jump(now);
        if (input.isPressed(SettingsManager.getKeyCode("P1_LIGHT"))) applyAttack(p1.lightAttack(now), p2, now);
        if (input.isPressed(SettingsManager.getKeyCode("P1_HEAVY"))) applyAttack(p1.heavyAttack(now), p2, now);
        if (input.isPressed(SettingsManager.getKeyCode("P1_KICK"))) applyAttack(p1.kickAttack(now), p2, now);
        if (input.isPressed(SettingsManager.getKeyCode("P1_SPECIAL"))) applyAttack(p1.specialAttack(now), p2, now);
        if (input.isPressed(SettingsManager.getKeyCode("P1_ULT"))) applyAttack(p1.ultimateAttack(now), p2, now);

        if (mode == GameMode.PVP) {
            p2.startMoveLeft(input.isPressed(SettingsManager.getKeyCode("P2_LEFT")));
            p2.startMoveRight(input.isPressed(SettingsManager.getKeyCode("P2_RIGHT")));
            p2.setCrouching(input.isPressed(SettingsManager.getKeyCode("P2_CROUCH")));
            p2.setBlocking(input.isPressed(SettingsManager.getKeyCode("P2_BLOCK")));
            if (input.isPressed(SettingsManager.getKeyCode("P2_JUMP"))) p2.jump(now);
            if (input.isPressed(SettingsManager.getKeyCode("P2_LIGHT"))) applyAttack(p2.lightAttack(now), p1, now);
            if (input.isPressed(SettingsManager.getKeyCode("P2_HEAVY"))) applyAttack(p2.heavyAttack(now), p1, now);
            if (input.isPressed(SettingsManager.getKeyCode("P2_KICK"))) applyAttack(p2.kickAttack(now), p1, now);
            if (input.isPressed(SettingsManager.getKeyCode("P2_SPECIAL"))) applyAttack(p2.specialAttack(now), p1, now);
            if (input.isPressed(SettingsManager.getKeyCode("P2_ULT"))) applyAttack(p2.ultimateAttack(now), p1, now);
        }
    }

    private void applyAttack(AttackResult result, Fighter target, long now) {
        if (result == null) return;
        Fighter source = result.source;
        Rectangle hit = source.getAttackBox(result.range);
        if (hit.intersects(target.getBodyBox())) {
            int before = target.getHealth();
            boolean blocked = target.isBlocking();
            target.takeHit(result.damage, result.stunMs, result.knockbackX, result.liftY, source.isFacingRight(), now);
            int dealt = Math.max(0, before - target.getHealth());
            if (dealt > 0) {
                source.restoreMana(Math.max(4, dealt / 3) + result.bonusManaOnHit);
            }
            if (!blocked && result.damage <= 12) { SoundPlayer.play("assets/sounds/punch.wav"); } else if (!blocked) { SoundPlayer.play("assets/sounds/hit.wav"); }
            double fx = target.getCenterX();
            double fy = target.getY() + 48;
            hitEffects.add(new HitEffect(fx, fy, result.stunMs > 0, result.knockbackX > 0, blocked));
            if (blocked) {
                SoundPlayer.play("assets/sounds/block.wav");
                eventAnnouncement = "You are Blocked bleeh";
                eventAnnouncementUntil = now + 1000;
                shakeStrength = 4;
                shakeUntil = now + 120;
            } else if (result.ultimateMove) {
                SoundPlayer.play("assets/sounds/ultimate.wav");
                eventAnnouncement = source.getName() + " used " + result.attackName + "!";
                eventAnnouncementUntil = now + 1200;
                shakeStrength = 12;
                shakeUntil = now + 220;
            } else if (result.specialMove) {
                SoundPlayer.play("assets/sounds/special.wav");
                eventAnnouncement = source.getName() + " used " + result.attackName + "!";
                eventAnnouncementUntil = now + 1100;
                shakeStrength = 8;
                shakeUntil = now + 180;
            } else if (result.knockbackX > 0) {
                SoundPlayer.play("assets/sounds/knockback.wav");
                eventAnnouncement = "Knockback!";
                eventAnnouncementUntil = now + 850;
                shakeStrength = result.stunMs > 0 ? 12 : 8;
                shakeUntil = now + 180;
            }
            if (source.getComboCounter() >= 2) {
                String comboTag = result.liftY > 0 ? "LAUNCHER" : (source.getComboCounter() >= 5 ? "FINISHER" : (target.getY() < Fighter.FLOOR_Y ? "AIR COMBO" : "CHAIN"));
                comboAnnouncement = source.getName() + " " + comboTag + " x" + source.getComboCounter() + "  -" + dealt + "  PUSH " + result.knockbackX;
                comboAnnouncementUntil = now + 1050;
            }
        }
    }

    private void updateEffects(long now) {
        Iterator<HitEffect> it = hitEffects.iterator();
        while (it.hasNext()) {
            if (it.next().expired(now)) {
                it.remove();
            }
        }
        if (now - lastParticleSpawn > 130) {
            moneyParticles.add(new MoneyParticle());
            glowParticles.add(new StageGlowParticle());
            if (moneyParticles.size() > 48) {
                moneyParticles.remove(0);
            }
            if (glowParticles.size() > 26) {
                glowParticles.remove(0);
            }
            lastParticleSpawn = now;
        }
        Iterator<MoneyParticle> particleIt = moneyParticles.iterator();
        while (particleIt.hasNext()) {
            MoneyParticle particle = particleIt.next();
            particle.update();
            if (particle.y > PANEL_H + 30) {
                particleIt.remove();
            }
        }
        Iterator<StageGlowParticle> glowIt = glowParticles.iterator();
        while (glowIt.hasNext()) {
            StageGlowParticle particle = glowIt.next();
            particle.update();
            if (particle.expired()) {
                glowIt.remove();
            }
        }
        stagePulse = (int) (60 + 50 * Math.abs(Math.sin(now / 430.0)));
    }

    private void updateTimer(long now) {
        if (now - lastSecondTick >= 1000) {
            countdown--;
            lastSecondTick += 1000;
        }
    }

    private void checkRoundEnd(long now) {
        if (roundOver) return;
        if (!p1.isAlive() || !p2.isAlive() || countdown <= 0) {
            roundOver = true;
            if (mode == GameMode.ARCADE) {
                if (p1.getHealth() == p2.getHealth()) {
                    startRoundIntroSequence("DRAW - ARCADE RUN ENDS");
                } else if (p1.getHealth() > p2.getHealth()) {
                    startRoundIntroSequence(p1.getName() + " CLEARS FLOOR " + arcadeFloor);
                } else {
                    startRoundIntroSequence(currentOpponentStats.name + " STOPS THE RUN");
                }
            } else if (p1.getHealth() == p2.getHealth()) {
                if (!p1.isAlive() && !p2.isAlive()) {
                    startRoundIntroSequence("DOUBLE KO");
                } else {
                    startRoundIntroSequence("DRAW");
                }
            } else if (p1.getHealth() > p2.getHealth()) {
                p1.addRoundWin();
                startRoundIntroSequence(p1.getName() + " TAKES ROUND " + roundNumber);
            } else {
                p2.addRoundWin();
                startRoundIntroSequence(p2.getName() + " TAKES ROUND " + roundNumber);
            }
            overlayUntil = now + 2200;
        }
    }

    private void resetRound() {
        p1.resetForRound(300);
        p2.resetForRound(1490);
        countdown = 60;
        lastSecondTick = System.currentTimeMillis();
        roundOver = false;
        hitEffects.clear();
        startRoundIntroSequence("ROUND " + roundNumber);
    }

    private void startRoundIntroSequence(String text) {
        overlayText = text + "  •  READY  •  FIGHT";
        overlayUntil = System.currentTimeMillis() + 1800;
    }

    private String getWinnerName() {
        return p1.getRoundsWon() > p2.getRoundsWon() ? p1.getName() : p2.getName();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight());
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        double scale = Math.min(getWidth() / (double) PANEL_W, getHeight() / (double) PANEL_H);
        int drawW = (int) Math.round(PANEL_W * scale);
        int drawH = (int) Math.round(PANEL_H * scale);
        int offsetX = (getWidth() - drawW) / 2;
        int offsetY = (getHeight() - drawH) / 2;
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        long now = System.currentTimeMillis();
        if (now < shakeUntil) {
            int dx = (int) (Math.sin(now / 18.0) * shakeStrength);
            int dy = (int) (Math.cos(now / 20.0) * Math.max(2, shakeStrength / 2));
            g2.translate(dx, dy);
        }

        g2.drawImage(stage, 0, 0, null);
        drawArena(g2);
        p1.draw(g2, 760);
        p2.draw(g2, 760);
        drawHitEffects(g2);
        drawHud(g2);

        if (System.currentTimeMillis() < overlayUntil) {
            g2.setFont(new Font("Arial", Font.BOLD, 34));
            g2.setColor(new Color(0, 0, 0, 155));
            g2.fillRoundRect(560, 430, 800, 96, 18, 18);
            g2.setColor(Color.WHITE);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (PANEL_W - fm.stringWidth(overlayText)) / 2;
            g2.drawString(overlayText, tx, 490);
        }
        if (paused) {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRect(0, 0, PANEL_W, PANEL_H);
        g2.drawImage(stage, 0, 0, PANEL_W, PANEL_H, null);
            g2.setColor(new Color(255, 220, 140));
            g2.setFont(new Font("Arial", Font.BOLD, 44));
            String pausedText = "PAUSED";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(pausedText, (PANEL_W - fm.stringWidth(pausedText)) / 2, 500);
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            String controls = "SPACE resume   •   ESC return to menu";
            FontMetrics fm2 = g2.getFontMetrics();
            g2.drawString(controls, (PANEL_W - fm2.stringWidth(controls)) / 2, 550);
        }
        g2.dispose();
    }

    private void drawArena(Graphics2D g2) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(4, 6, 18), 0, 360, new Color(18, 18, 40));
        g2.setPaint(sky);
        g2.fillRect(0, 0, PANEL_W, PANEL_H);
        g2.drawImage(stage, 0, 0, PANEL_W, PANEL_H, null);

        int skylineTop = 210;
        g2.setColor(new Color(16, 18, 36, 230));
        for (int x = 0; x < PANEL_W; x += 120) {
            int bw = 88;
            int bh = 120 + (x % 5) * 28;
            g2.fillRect(x + 8, skylineTop + 120 - bh, bw, bh);
            g2.setColor(new Color(245, 210, 110, 210));
            for (int wx = x + 18; wx < x + bw; wx += 14) {
                for (int wy = skylineTop + 130 - bh; wy < skylineTop + 110; wy += 16) {
                    g2.fillRect(wx, wy, 6, 10);
                }
            }
            g2.setColor(new Color(16, 18, 36, 230));
        }

        int hallW = 600;
        int hallH = 330;
        int hallX = (PANEL_W - hallW) / 2;
        int hallY = 170;

        g2.setColor(new Color(255, 223, 120, 58 + stagePulse / 4));
        g2.fillOval(PANEL_W / 2 - 390, hallY - 120, 780, 250);
        g2.setColor(new Color(255, 235, 170, 36));
        g2.fillOval(PANEL_W / 2 - 560, hallY - 100, 1120, 220);

        g2.setColor(new Color(10, 12, 18, 150));
        g2.fillRoundRect(hallX - 28, hallY - 20, hallW + 56, hallH + 54, 30, 30);
        g2.setColor(new Color(255, 214, 120, 70));
        g2.fillRoundRect(hallX, hallY, hallW, hallH, 20, 20);
        g2.setColor(new Color(255, 214, 120, 190));
        g2.setStroke(new BasicStroke(3.4f));
        g2.drawRoundRect(hallX, hallY, hallW, hallH, 20, 20);
        g2.drawRect(hallX + 48, hallY + 78, hallW - 96, hallH - 112);
        for (int i = 0; i < 6; i++) {
            int cx = hallX + 62 + i * 60;
            g2.drawLine(cx, hallY + 78, cx, hallY + hallH - 34);
        }
        g2.drawLine(hallX + 26, hallY + 78, hallX + hallW - 26, hallY + 78);
        Polygon pediment = new Polygon();
        pediment.addPoint(PANEL_W / 2, hallY - 48);
        pediment.addPoint(hallX + 24, hallY + 30);
        pediment.addPoint(hallX + hallW - 24, hallY + 30);
        g2.fillPolygon(pediment);
        g2.setColor(new Color(255, 214, 120, 168));
        g2.drawPolygon(pediment);

        g2.setColor(new Color(145, 245, 150, 140));
        g2.fillRect(hallX + hallW + 56, hallY + 10, 52, 42);
        g2.rotate(Math.toRadians(-14), hallX + hallW + 82, hallY + 30);
        g2.fillRect(hallX + hallW + 36, hallY + 22, 62, 24);
        g2.rotate(Math.toRadians(14), hallX + hallW + 82, hallY + 30);

        g2.setColor(new Color(255, 210, 110, 190));
        g2.fillOval(180, 430, 64, 64);
        g2.fillOval(PANEL_W - 244, 430, 64, 64);
        g2.setStroke(new BasicStroke(5f));
        g2.drawLine(212, 494, 212, 720);
        g2.drawLine(PANEL_W - 212, 494, PANEL_W - 212, 720);

        GradientPaint mid = new GradientPaint(0, 620, new Color(22, 20, 54, 240), 0, 780, new Color(15, 12, 32, 245));
        g2.setPaint(mid);
        g2.fillRect(0, 650, PANEL_W, 140);
        g2.setColor(new Color(210, 178, 94, 120));
        g2.drawLine(0, 650, PANEL_W, 650);
        g2.setColor(new Color(255, 230, 150, 100));
        g2.setFont(new Font("Arial", Font.BOLD, 26));
        FontMetrics hallFm = g2.getFontMetrics();
        String hallLabel = "MIDNIGHT GOVERNMENT HALL";
        g2.drawString(hallLabel, (PANEL_W - hallFm.stringWidth(hallLabel)) / 2, hallY + hallH + 70);

        GradientPaint floor = new GradientPaint(0, 790, new Color(74, 10, 28, 240), 0, PANEL_H, new Color(18, 14, 28, 255));
        g2.setPaint(floor);
        g2.fillRect(0, 790, PANEL_W, PANEL_H - 790);
        g2.setColor(new Color(196, 160, 75, 180));
        g2.drawLine(0, 790, PANEL_W, 790);

        g2.setColor(new Color(255, 220, 140, 35));
        for (int x = 70; x < PANEL_W; x += 120) {
            g2.drawLine(x, 790, x + 70, PANEL_H - 10);
        }

        g2.setColor(new Color(180, 140, 58, 120));
        g2.drawArc(PANEL_W / 2 - 160, 760, 320, 110, 0, 180);
        g2.drawLine(PANEL_W / 2, 760, PANEL_W / 2 - 24, 870);
        g2.drawLine(PANEL_W / 2, 760, PANEL_W / 2 + 24, 870);

        for (StageGlowParticle particle : glowParticles) {
            particle.draw(g2);
        }
        for (MoneyParticle particle : moneyParticles) {
            particle.draw(g2);
        }

        g2.setColor(new Color(0, 0, 0, 145));
        g2.fillRoundRect(20, 20, PANEL_W - 40, 150, 24, 24);
        g2.setColor(new Color(210, 182, 98, 170));
        g2.drawRoundRect(20, 20, PANEL_W - 40, 150, 24, 24);
    }

    private void drawHitEffects(Graphics2D g2) {
        long now = System.currentTimeMillis();
        for (HitEffect effect : hitEffects) {
            float ratio = effect.ratio(now);
            int alpha = (int) (220 * (1f - ratio));
            int size = 22 + (int) (18 * ratio);
            g2.setColor(new Color(255, 245, 140, Math.max(0, alpha)));
            g2.fillOval((int) effect.x - size / 2, (int) effect.y - size / 2, size, size);
            g2.setColor(new Color(effect.blocked ? 110 : 255, effect.blocked ? 210 : 120, 255, Math.max(0, alpha)));
            g2.drawOval((int) effect.x - size, (int) effect.y - size, size * 2, size * 2);
            if (effect.knockback) {
                g2.setColor(new Color(255, 170, 80, Math.max(0, alpha)));
                g2.drawArc((int) effect.x - size - 12, (int) effect.y - size / 2, size * 2, size, 330, 100);
                g2.drawArc((int) effect.x - size - 20, (int) effect.y - size / 2 - 6, size * 2, size, 330, 100);
            }
            if (effect.ultimate) {
                g2.setColor(new Color(255, 90, 90, Math.max(0, alpha)));
                g2.drawLine((int) effect.x - size, (int) effect.y - size, (int) effect.x + size, (int) effect.y + size);
                g2.drawLine((int) effect.x + size, (int) effect.y - size, (int) effect.x - size, (int) effect.y + size);
            }
        }
    }

    private void drawHud(Graphics2D g2) {
        long now = System.currentTimeMillis();
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        drawPortraitFrame(g2, 28, 28, p1Portrait, p1, true);
        drawPortraitFrame(g2, PANEL_W - 112, 28, p2Portrait, p2, false);
        drawHealthBar(g2, 124, 40, 560, 28, p1, true);
        drawHealthBar(g2, 1236, 40, 560, 28, p2, false);
        drawManaBar(g2, 124, 76, 560, 18, p1, true);
        drawManaBar(g2, 1236, 76, 560, 18, p2, false);
        drawSkillIcons(g2, 124, 102, p1, true);
        drawSkillIcons(g2, 1796, 102, p2, false);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));
        String timerText = String.valueOf(countdown);
        FontMetrics tfm = g2.getFontMetrics();
        g2.drawString(timerText, (PANEL_W - tfm.stringWidth(timerText)) / 2, 68);

        g2.setFont(new Font("Arial", Font.BOLD, 22));
        String roundText = "ROUND " + roundNumber;
        FontMetrics rfm = g2.getFontMetrics();
        g2.drawString(roundText, (PANEL_W - rfm.stringWidth(roundText)) / 2, 112);
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString("P1 Rounds: " + p1.getRoundsWon(), 124, 136);
        String p2Rounds = "P2 Rounds: " + p2.getRoundsWon();
        g2.drawString(p2Rounds, PANEL_W - 56 - g2.getFontMetrics().stringWidth(p2Rounds), 136);

        drawMoveCard(g2, 48, 900, 760, 132, p1, true);
        drawMoveCard(g2, PANEL_W - 808, 900, 760, 132, p2, false);

        g2.setColor(new Color(235, 212, 130));
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        String banner = mode == GameMode.ARCADE
                ? "Arcade survival • " + arcadeSetting.label + " • Boss every 5 floors • " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("PAUSE")) + " pauses • " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("BACK")) + " returns to menu"
                : "Midnight Government Hall dominates center stage • " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("PAUSE")) + " pauses • " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("BACK")) + " returns to menu";
        FontMetrics bfm = g2.getFontMetrics();
        g2.drawString(banner, (PANEL_W - bfm.stringWidth(banner)) / 2, 160);

        if (mode == GameMode.ARCADE) {
            drawArcadeStatus(g2);
        }

        if (now < comboAnnouncementUntil) {
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(comboAnnouncement) + 34;
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRoundRect((PANEL_W - w) / 2, 176, w, 42, 14, 14);
            g2.setColor(new Color(255, 225, 110));
            g2.drawString(comboAnnouncement, (PANEL_W - fm.stringWidth(comboAnnouncement)) / 2, 204);
        }
        if (now < eventAnnouncementUntil) {
            g2.setFont(new Font("Arial", Font.BOLD, 22));
            FontMetrics fm = g2.getFontMetrics();
            int w = fm.stringWidth(eventAnnouncement) + 34;
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRoundRect((PANEL_W - w) / 2, 232, w, 40, 14, 14);
            g2.setColor(eventAnnouncement.contains("Blocked") ? new Color(150, 235, 255) : (eventAnnouncement.contains("used") ? new Color(255, 220, 120) : new Color(255, 170, 90)));
            g2.drawString(eventAnnouncement, (PANEL_W - fm.stringWidth(eventAnnouncement)) / 2, 259);
        }
    }


    private void drawArcadeStatus(Graphics2D g2) {
        int x = 36;
        int y = 186;
        int w = 520;
        int h = 130;
        g2.setColor(new Color(8, 10, 18, 200));
        g2.fillRoundRect(x, y, w, h, 18, 18);
        g2.setColor(new Color(245, 190, 70, 160));
        g2.drawRoundRect(x, y, w, h, 18, 18);
        g2.setColor(new Color(255, 211, 108));
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString("ARCADE SURVIVAL", x + 16, y + 28);
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        g2.drawString("Difficulty: " + arcadeSetting.label + "   Floors cleared: " + arcadeWins + "   Current floor: " + arcadeFloor, x + 16, y + 54);
        g2.drawString("AI tier: " + arcadeDifficulty + "   Continues left: " + continuesRemaining + "   Next boss: floor " + (((arcadeFloor - 1) / 5) * 5 + 5), x + 16, y + 78);
        g2.drawString(bossFloor ? "Boss floor active: expect higher HP, stronger knockback, and longer stun." : "After each win you recover some HP, mana, and refresh cooldowns.", x + 16, y + 102);
        g2.setColor(new Color(170, 215, 255));
        g2.drawString(beginnerHint, x + 16, y + 122);
    }

    private void drawPortraitFrame(Graphics2D g2, int x, int y, BufferedImage portrait, Fighter fighter, boolean left) {
        g2.setColor(new Color(8, 12, 20, 220));
        g2.fillRoundRect(x, y, 84, 84, 20, 20);
        int pulse = fighter.isManaFull() ? 180 + (int) (50 * Math.abs(Math.sin(System.currentTimeMillis() / 170.0))) : 150;
        g2.setColor(new Color(245, 190, 70, pulse));
        g2.drawRoundRect(x, y, 84, 84, 20, 20);
        g2.drawImage(portrait, x + 6, y + 6, 72, 72, null);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(Color.WHITE);
        String label = left ? "P1" : (mode == GameMode.PVP ? "P2" : "CPU");
        g2.drawString(label, x + 8, y + 16);
        if (fighter.isStunned(System.currentTimeMillis())) {
            g2.setColor(new Color(255, 235, 95));
            g2.drawString("STUN", x + 46, y + 16);
        }
    }

    private void drawMoveCard(Graphics2D g2, int x, int y, int w, int h, Fighter fighter, boolean playerOne) {
        g2.setColor(new Color(8, 10, 18, 190));
        g2.fillRoundRect(x, y, w, h, 18, 18);
        g2.setColor(new Color(245, 190, 70, 160));
        g2.drawRoundRect(x, y, w, h, 18, 18);
        g2.setColor(new Color(255, 211, 108));
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.drawString(fighter.getName(), x + 16, y + 24);
        g2.setFont(new Font("Arial", Font.PLAIN, 15));
        String line1 = playerOne
                ? "Move: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_LEFT")) + "/" + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_RIGHT")) + "   Jump: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_JUMP")) + "   Crouch: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_CROUCH")) + "   Block: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_BLOCK")) + "   Attacks: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_LIGHT")) + " / " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_HEAVY")) + " / " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_KICK"))
                : "Move: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_LEFT")) + "/" + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_RIGHT")) + "   Jump: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_JUMP")) + "   Crouch: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_CROUCH")) + "   Block: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_BLOCK")) + "   Attacks: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_LIGHT")) + " / " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_HEAVY")) + " / " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_KICK"));
        String line2 = playerOne
                ? "Skill: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_SPECIAL")) + "  " + fighter.getSpecialName() + " (" + fighter.getSpecialCost() + " MP)"
                : "Skill: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_SPECIAL")) + "  " + fighter.getSpecialName() + " (" + fighter.getSpecialCost() + " MP)";
        String line3 = playerOne
                ? "Ultimate: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P1_ULT")) + "  " + fighter.getUltimateName() + " (" + fighter.getUltimateCost() + " MP)"
                : "Ultimate: " + java.awt.event.KeyEvent.getKeyText(SettingsManager.getKeyCode("P2_ULT")) + "  " + fighter.getUltimateName() + " (" + fighter.getUltimateCost() + " MP)";
        g2.setColor(Color.WHITE);
        g2.drawString(line1, x + 16, y + 50);
        g2.drawString(line2, x + 16, y + 74);
        g2.drawString(line3, x + 16, y + 98);
        g2.setColor(new Color(170, 215, 255));
        String tag = fighter.getSpecialTag() + " • " + fighter.getUltimateTag();
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(tag, x + w - fm.stringWidth(tag) - 16, y + 24);
    }

    private void drawManaBar(Graphics2D g2, int x, int y, int w, int h, Fighter fighter, boolean leftToRight) {
        g2.setColor(new Color(14, 24, 44, 210));
        g2.fillRoundRect(x, y, w, h, 8, 8);
        double ratio = fighter.getMana() / (double) fighter.getMaxMana();
        int fill = (int) (w * ratio);
        if (fighter.isManaFull()) {
            int glow = 130 + (int) (70 * Math.abs(Math.sin(System.currentTimeMillis() / 160.0)));
            g2.setColor(new Color(90, 190, 255, glow));
            g2.fillRoundRect(x - 2, y - 2, w + 4, h + 4, 10, 10);
        }
        g2.setColor(new Color(90, 190, 255));
        if (leftToRight) {
            g2.fillRoundRect(x, y, fill, h, 8, 8);
        } else {
            g2.fillRoundRect(x + (w - fill), y, fill, h, 8, 8);
        }
        g2.setColor(new Color(225, 245, 255));
        g2.drawRoundRect(x, y, w, h, 8, 8);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString("MP " + fighter.getMana() + "/" + fighter.getMaxMana(), x + 6, y + h - 2);
        if (fighter.isManaFull()) {
            g2.setColor(new Color(180, 235, 255));
            FontMetrics fm3 = g2.getFontMetrics();
            g2.drawString("FULL MANA", x + w - fm3.stringWidth("FULL MANA") - 6, y + h - 2);
        }
    }

    private void drawSkillIcons(Graphics2D g2, int anchorX, int y, Fighter fighter, boolean leftAligned) {
        int iconSize = 22;
        int gap = 8;
        int x1 = leftAligned ? anchorX : anchorX - (iconSize * 2 + gap);
        drawSkillIcon(g2, x1, y, iconSize, "SP", fighter.isSpecialReady(), fighter.getSpecialCost(), fighter.getSpecialCooldownRemaining(System.currentTimeMillis()));
        drawSkillIcon(g2, x1 + iconSize + gap, y, iconSize, "ULT", fighter.isUltimateReady(), fighter.getUltimateCost(), fighter.getUltimateCooldownRemaining(System.currentTimeMillis()));
    }

    private void drawSkillIcon(Graphics2D g2, int x, int y, int size, String label, boolean ready, int cost, long cooldownRemaining) {
        g2.setColor(new Color(8, 12, 20, 220));
        g2.fillRoundRect(x, y, size, size, 8, 8);
        if (ready) {
            int pulse = 110 + (int) (70 * Math.abs(Math.sin(System.currentTimeMillis() / 170.0)));
            g2.setColor(new Color(90, 190, 255, pulse));
            g2.fillRoundRect(x + 2, y + 2, size - 4, size - 4, 8, 8);
        } else if (cooldownRemaining > 0) {
            int overlayH = (int) ((size - 4) * Math.min(1.0, cooldownRemaining / 6000.0));
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillRoundRect(x + 2, y + 2, size - 4, overlayH, 8, 8);
        }
        g2.setColor(ready ? new Color(220, 245, 255) : new Color(140, 148, 165));
        g2.drawRoundRect(x, y, size, size, 8, 8);
        g2.setFont(new Font("Arial", Font.BOLD, label.length() > 2 ? 9 : 10));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(label, x + (size - fm.stringWidth(label)) / 2, y + 11);
        g2.setFont(new Font("Arial", Font.BOLD, 8));
        String c = String.valueOf(cost);
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(c, x + (size - fm2.stringWidth(c)) / 2, y + 19);
    }

    private void drawHealthBar(Graphics2D g2, int x, int y, int w, int h, Fighter fighter, boolean leftToRight) {
        g2.setColor(new Color(20,20,20,210));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        double ratio = fighter.getHealth() / (double) fighter.getMaxHealth();
        int fill = (int) (w * ratio);

        GradientPaint hpPaint = new GradientPaint(x, y, new Color(80, 230, 120), x + w, y, new Color(255, 120, 95));
        g2.setPaint(hpPaint);
        if (leftToRight) {
            g2.fillRoundRect(x, y, fill, h, 10, 10);
        } else {
            g2.fillRoundRect(x + (w - fill), y, fill, h, 10, 10);
        }
        g2.setColor(Color.WHITE);
        g2.drawRoundRect(x, y, w, h, 10, 10);
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.drawString(fighter.getName(), x, y - 8);
        String hpText = fighter.getHealth() + "/" + fighter.getMaxHealth();
        FontMetrics fm = g2.getFontMetrics();
        int tx = leftToRight ? x + w - fm.stringWidth(hpText) - 10 : x + 10;
        g2.drawString(hpText, tx, y - 8);
    }



    private static class StageGlowParticle {
        double x = 760 + Math.random() * 420;
        double y = 140 + Math.random() * 220;
        double vx = -0.25 + Math.random() * 0.5;
        double vy = 0.4 + Math.random() * 1.1;
        int size = 8 + (int) (Math.random() * 18);
        int life = 70 + (int) (Math.random() * 70);

        void update() {
            x += vx;
            y += vy;
            life--;
        }

        boolean expired() {
            return life <= 0 || y > PANEL_H + 20;
        }

        void draw(Graphics2D g2) {
            int alpha = Math.max(0, Math.min(150, life * 2));
            g2.setColor(new Color(255, 220, 120, alpha));
            g2.fillOval((int) x, (int) y, size, size);
        }
    }

    private static class MoneyParticle {
        double x = 120 + Math.random() * (PANEL_W - 240);
        double y = -20 - Math.random() * 180;
        double speed = 1.3 + Math.random() * 2.2;
        double sway = -0.8 + Math.random() * 1.6;
        int size = 10 + (int) (Math.random() * 8);
        int alpha = 90 + (int) (Math.random() * 80);

        void update() {
            y += speed;
            x += sway;
        }

        void draw(Graphics2D g2) {
            g2.setColor(new Color(154, 220, 140, alpha));
            g2.fillRoundRect((int) x, (int) y, size + 6, size, 3, 3);
            g2.setColor(new Color(225, 245, 200, Math.max(80, alpha - 20)));
            g2.drawLine((int) x + 3, (int) y + size / 2, (int) x + size + 1, (int) y + size / 2);
        }
    }

    private static class HitEffect {
        final double x;
        final double y;
        final boolean ultimate;
        final boolean knockback;
        final boolean blocked;
        final long born = System.currentTimeMillis();
        final long durationMs;

        HitEffect(double x, double y, boolean ultimate, boolean knockback, boolean blocked) {
            this.x = x;
            this.y = y;
            this.ultimate = ultimate;
            this.knockback = knockback;
            this.blocked = blocked;
            this.durationMs = ultimate ? 500 : 260;
        }

        boolean expired(long now) {
            return now - born > durationMs;
        }

        float ratio(long now) {
            return Math.min(1f, (now - born) / (float) durationMs);
        }
    }
}
