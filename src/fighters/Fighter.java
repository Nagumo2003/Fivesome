package fighters;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import util.AssetLoader;
import util.GameMath;

public class Fighter {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 270;
    public static final double GRAVITY = 0.92;
    public static final double FLOOR_Y = 690;

    private final FighterStats stats;
    private final boolean facingRightDefault;
    private double x;
    private double y = FLOOR_Y;
    private double vx;
    private double vy;
    private boolean moveLeft;
    private boolean moveRight;
    private boolean crouching;
    private boolean blocking;
    private boolean jumping;
    private boolean wasAttackApplied;
    private ActionType action = ActionType.IDLE;
    private int actionTimer;
    private int comboCounter;
    private long comboWindowUntil;
    private long stunUntil;
    private int health;
    private int mana;
    private int roundsWon;
    private boolean aiControlled;
    private BufferedImage sprite;
    private BufferedImage spriteSheet;
    private int sheetFrames;
    private long lastManaTick;
    private long manaFullGlowUntil;
    private long manaSpentFlashUntil;
    private long specialCooldownUntil;
    private long ultimateCooldownUntil;

    public Fighter(FighterStats stats, double x, boolean facingRightDefault, String spritePath) {
        this.stats = stats;
        this.x = x;
        this.facingRightDefault = facingRightDefault;
        this.health = stats.maxHealth;
        this.mana = Math.max(stats.specialCost, stats.maxMana / 2 + 5);
        this.sprite = AssetLoader.loadImage(spritePath, WIDTH, HEIGHT);
        String sheetPath = spritePath.replace("/sprites/", "/sheets/").replace(".png", "_sheet.png");
        this.spriteSheet = AssetLoader.loadImage(sheetPath, WIDTH * 4, HEIGHT);
        this.sheetFrames = Math.max(1, this.spriteSheet.getWidth() / Math.max(1, WIDTH));
        this.lastManaTick = System.currentTimeMillis();
        this.manaFullGlowUntil = 0;
        this.manaSpentFlashUntil = 0;
        this.specialCooldownUntil = 0;
        this.ultimateCooldownUntil = 0;
    }

    public void resetForRound(double spawnX) {
        resetPositionOnly(spawnX);
        this.health = stats.maxHealth;
        this.mana = Math.max(stats.specialCost, stats.maxMana / 2 + 5);
        this.specialCooldownUntil = 0;
        this.ultimateCooldownUntil = 0;
    }

    public void resetPositionOnly(double spawnX) {
        this.x = spawnX;
        this.y = FLOOR_Y;
        this.vx = 0;
        this.vy = 0;
        this.moveLeft = false;
        this.moveRight = false;
        this.crouching = false;
        this.blocking = false;
        this.jumping = false;
        this.action = ActionType.IDLE;
        this.actionTimer = 0;
        this.stunUntil = 0;
        this.wasAttackApplied = false;
        this.comboCounter = 0;
        this.comboWindowUntil = 0;
        this.lastManaTick = System.currentTimeMillis();
        this.manaFullGlowUntil = 0;
        this.manaSpentFlashUntil = 0;
    }

    public void update(long nowMs, int arenaWidth) {
        if (health <= 0) {
            action = ActionType.KO;
            vx = 0;
        }
        boolean stunned = nowMs < stunUntil;
        if (stunned) {
            moveLeft = false;
            moveRight = false;
            blocking = false;
            crouching = false;
        }

        if (actionTimer > 0) {
            actionTimer--;
            if (actionTimer == 0 && health > 0) {
                if (y < FLOOR_Y) {
                    action = ActionType.JUMP;
                } else if (crouching) {
                    action = ActionType.CROUCH;
                } else if (moveLeft || moveRight) {
                    action = ActionType.WALK;
                } else {
                    action = ActionType.IDLE;
                }
                wasAttackApplied = false;
            }
        }

        if (!stunned && !isLockedInAction()) {
            vx *= 0.82;
            if (Math.abs(vx) < 0.35) vx = 0;
            if (moveLeft) vx -= stats.speed;
            if (moveRight) vx += stats.speed;
            if (vx != 0 && y >= FLOOR_Y && !crouching) {
                action = ActionType.WALK;
            } else if (y >= FLOOR_Y && !crouching && !blocking) {
                action = ActionType.IDLE;
            }
            if (blocking && y >= FLOOR_Y) action = ActionType.BLOCK;
            if (crouching && y >= FLOOR_Y) action = ActionType.CROUCH;
        }

        x += vx;
        vy += GRAVITY;
        y += vy;
        if (y >= FLOOR_Y) {
            y = FLOOR_Y;
            vy = 0;
            jumping = false;
            if (action == ActionType.JUMP && actionTimer <= 0) action = ActionType.IDLE;
        }

        x = GameMath.clamp(x, 20, arenaWidth - WIDTH - 20);
        if ((x <= 20 || x >= arenaWidth - WIDTH - 20) && Math.abs(vx) > 0.2) vx *= 0.35;
        if (comboWindowUntil < nowMs) comboCounter = 0;

        if (nowMs - lastManaTick >= 200) {
            int ticks = (int) ((nowMs - lastManaTick) / 200);
            restoreMana(ticks);
            lastManaTick += ticks * 200L;
        }
        if (mana >= stats.maxMana) {
            manaFullGlowUntil = Math.max(manaFullGlowUntil, nowMs + 120);
        }
    }

    public boolean canAct(long nowMs) {
        return health > 0 && nowMs >= stunUntil && !isLockedInAction();
    }

    public void jump(long nowMs) {
        if (canAct(nowMs) && !jumping && y >= FLOOR_Y) {
            vy = -14.0;
            jumping = true;
            action = ActionType.JUMP;
            actionTimer = 10;
        }
    }

    public void startMoveLeft(boolean v) { this.moveLeft = v; }
    public void startMoveRight(boolean v) { this.moveRight = v; }
    public void setCrouching(boolean v) { this.crouching = v; }
    public void setBlocking(boolean v) { this.blocking = v; }

    public AttackResult lightAttack(long nowMs) {
        return beginAttack(nowMs, ActionType.LIGHT, stats.lightDamage, 18, 45, 0, 12, 0, 0, 3,
                "Jab", false, false, 0, 0);
    }

    public AttackResult heavyAttack(long nowMs) {
        return beginAttack(nowMs, ActionType.HEAVY, stats.heavyDamage, 24, 55, 0, 20, 0, 0, 4,
                "Heavy Smash", false, false, 0, 0);
    }

    public AttackResult kickAttack(long nowMs) {
        return beginAttack(nowMs, ActionType.KICK, stats.kickDamage, 20, 55, 0, 16, 5, 0, 5,
                "Kick", false, false, 0, 0);
    }

    public AttackResult specialAttack(long nowMs) {
        if (nowMs < specialCooldownUntil) return null;
        AttackResult result = beginAttack(nowMs, ActionType.SPECIAL, stats.specialDamage, 28,
                stats.specialRange, stats.specialStunMs, stats.specialKnockback, stats.specialLift,
                stats.specialCost, 0, stats.specialName, true, false, stats.specialManaRefundOnHit,
                stats.specialCooldownMs);
        if (result != null) specialCooldownUntil = nowMs + stats.specialCooldownMs;
        return result;
    }

    public AttackResult ultimateAttack(long nowMs) {
        if (nowMs < ultimateCooldownUntil) return null;
        AttackResult result = beginAttack(nowMs, ActionType.ULTIMATE, stats.ultimateDamage, 36,
                stats.ultimateRange, stats.ultimateStunMs, stats.ultimateKnockback, stats.ultimateLift,
                stats.ultimateCost, 0, stats.ultimateName, false, true, stats.ultimateManaRefundOnHit,
                stats.ultimateCooldownMs);
        if (result != null) ultimateCooldownUntil = nowMs + stats.ultimateCooldownMs;
        return result;
    }

    private AttackResult beginAttack(long nowMs, ActionType type, int damage, int duration, int range,
                                     int stunMs, int knockbackX, int liftY, int manaCost, int manaGain,
                                     String attackName, boolean specialMove, boolean ultimateMove,
                                     int bonusManaOnHit, int cooldownMs) {
        if (!canAct(nowMs)) return null;
        if (manaCost > 0 && mana < manaCost) return null;
        if (manaCost > 0) {
            mana = Math.max(0, mana - manaCost);
            manaSpentFlashUntil = nowMs + 280;
        }
        if (manaGain > 0) restoreMana(manaGain);
        action = type;
        actionTimer = duration;
        wasAttackApplied = false;
        comboCounter = (comboWindowUntil >= nowMs) ? comboCounter + 1 : 1;
        comboWindowUntil = nowMs + 900;
        int comboStep = Math.max(0, comboCounter - 1);
        int bonus = Math.min(4, comboStep) * 2;
        int comboBoost = Math.min(18, comboStep * 3);
        double scaling = comboStep <= 1 ? 1.0 : Math.max(0.55, 1.0 - comboStep * 0.08);
        int scaledDamage = Math.max(1, (int) Math.round((damage + bonus) * scaling));
        return new AttackResult(scaledDamage, range, stunMs, knockbackX + comboBoost, liftY, this,
                attackName, specialMove, ultimateMove, bonusManaOnHit);
    }

    public void restoreMana(int amount) {
        if (amount <= 0) return;
        int before = mana;
        mana = Math.min(stats.maxMana, mana + amount);
        if (mana >= stats.maxMana && before < stats.maxMana) {
            manaFullGlowUntil = System.currentTimeMillis() + 650;
        }
    }

    public Rectangle getBodyBox() {
        return new Rectangle((int) x + 24, (int) y + 28, WIDTH - 48, HEIGHT - 34);
    }

    public Rectangle getAttackBox(int range) {
        int attackY = (int) y + 72;
        if (isFacingRight()) {
            return new Rectangle((int) x + WIDTH - 8, attackY, range, 35);
        }
        return new Rectangle((int) x - range + 8, attackY, range, 35);
    }

    public boolean isFacingRight() {
        if (moveLeft && !moveRight) return false;
        if (moveRight && !moveLeft) return true;
        return facingRightDefault;
    }

    public int getActionProgress() {
        return switch (action) {
            case LIGHT -> 12;
            case HEAVY -> 16;
            case KICK -> 14;
            case SPECIAL -> 18;
            case ULTIMATE -> 24;
            default -> 10;
        };
    }

    public void markAttackApplied() {
        wasAttackApplied = true;
    }

    private int getAnimationFrame(long nowMs) {
        int speed = switch (action) {
            case WALK -> 120;
            case LIGHT, HEAVY, KICK -> 85;
            case SPECIAL -> 70;
            case ULTIMATE -> 60;
            case HURT -> 100;
            case KO -> 180;
            default -> 170;
        };
        return (int) ((nowMs / speed) % Math.max(1, sheetFrames));
    }

    private BufferedImage getCurrentFrame(long nowMs) {
        if (spriteSheet == null || sheetFrames <= 1) return sprite;
        int frame = getAnimationFrame(nowMs);
        int frameWidth = Math.max(1, spriteSheet.getWidth() / sheetFrames);
        int sx = Math.min(spriteSheet.getWidth() - frameWidth, frame * frameWidth);
        BufferedImage sub = spriteSheet.getSubimage(sx, 0, frameWidth, spriteSheet.getHeight());
        BufferedImage scaled = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(sub, 0, 0, WIDTH, HEIGHT, null);
        g.dispose();
        return scaled;
    }

    private int getPoseOffsetY(long nowMs) {
        return switch (action) {
            case IDLE -> (int) Math.round(Math.sin(nowMs / 160.0) * 4);
            case WALK -> (int) Math.round(Math.sin(nowMs / 95.0) * 8);
            case JUMP -> -12;
            case CROUCH -> 24;
            case HURT -> 10;
            case KO -> 42;
            default -> 0;
        };
    }

    private int getPoseOffsetX(long nowMs) {
        return switch (action) {
            case LIGHT, HEAVY, KICK -> isFacingRight() ? 10 : -10;
            case SPECIAL -> isFacingRight() ? 18 : -18;
            case ULTIMATE -> isFacingRight() ? 24 : -24;
            default -> 0;
        };
    }

    public void takeHit(int damage, int stunMs, int knockbackX, int liftY, boolean hitFromRight, long nowMs) {
        if (blocking && y >= FLOOR_Y) {
            damage = Math.max(1, damage / 3);
            stunMs = 0;
            knockbackX = Math.max(4, knockbackX / 3);
            liftY = 0;
            restoreMana(3);
        }
        health -= damage;
        int direction = hitFromRight ? 1 : -1;
        vx = direction * knockbackX;
        if (liftY > 0 && y >= FLOOR_Y) {
            vy = -liftY;
            jumping = true;
        }
        if (health <= 0) {
            health = 0;
            action = ActionType.KO;
            actionTimer = 120;
        } else {
            action = ActionType.HURT;
            actionTimer = 14;
            stunUntil = Math.max(stunUntil, nowMs + Math.max(130, stunMs / 3));
        }
        if (stunMs > 0) {
            stunUntil = Math.max(stunUntil, nowMs + stunMs);
        }
    }

    public void draw(Graphics2D g2, int groundY) {
        long now = System.currentTimeMillis();
        BufferedImage frame = getCurrentFrame(now);
        int drawX = (int) x + getPoseOffsetX(now);
        int drawY = (int) y + getPoseOffsetY(now);

        if (!isFacingRight()) {
            AffineTransform old = g2.getTransform();
            g2.translate(drawX + WIDTH, drawY);
            g2.scale(-1, 1);
            g2.drawImage(frame, 0, 0, null);
            g2.setTransform(old);
        } else {
            g2.drawImage(frame, drawX, drawY, null);
        }

        if (action == ActionType.LIGHT || action == ActionType.HEAVY || action == ActionType.KICK) {
            g2.setColor(new Color(255, 220, 120, 70));
            int reach = action == ActionType.HEAVY ? 70 : 46;
            int rx = isFacingRight() ? drawX + WIDTH - 26 : drawX - reach + 26;
            g2.fillRoundRect(rx, drawY + 80, reach, 22, 16, 16);
        }
        if (action == ActionType.SPECIAL || action == ActionType.ULTIMATE) {
            g2.setColor(new Color(action == ActionType.ULTIMATE ? 255 : 120, 180, 255, action == ActionType.ULTIMATE ? 110 : 80));
            g2.fillOval(drawX - 12, drawY - 8, WIDTH + 24, HEIGHT + 18);
        }

        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(new Color(255,255,255,220));
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 22));
        g2.drawString(stats.name, (int)x - 2, (int)y - 10);
        if (action == ActionType.BLOCK) {
            g2.setColor(new Color(120, 200, 255, 120));
            Rectangle body = getBodyBox();
            g2.fillRect(body.x, body.y, body.width, body.height);
        }
        if (isManaFull()) {
            int pulse = 70 + (int) (40 * Math.abs(Math.sin(System.currentTimeMillis() / 180.0)));
            g2.setColor(new Color(70, 180, 255, pulse));
            g2.drawOval((int) x - 10, (int) y - 10, WIDTH + 20, HEIGHT + 20);
            g2.drawOval((int) x - 18, (int) y - 18, WIDTH + 36, HEIGHT + 36);
        } else if (System.currentTimeMillis() < manaSpentFlashUntil) {
            g2.setColor(new Color(120, 220, 255, 90));
            g2.fillOval((int) x + 8, (int) y + 10, WIDTH - 16, HEIGHT - 8);
        }
        if (isStunned(System.currentTimeMillis())) {
            g2.setColor(new Color(255, 230, 80));
            g2.drawString("STUN", (int)x + 26, (int)y - 28);
        }
        g2.setColor(new Color(110, 215, 255));
        g2.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 18));
        g2.drawString("MP " + mana + "/" + stats.maxMana, (int)x + 10, (int)y + HEIGHT + 18);
    }



    public void heal(int amount) {
        if (amount <= 0) return;
        health = Math.min(stats.maxHealth, health + amount);
    }

    public void refillMana(int amount) {
        restoreMana(amount);
    }

    public void resetCooldowns() {
        specialCooldownUntil = 0;
        ultimateCooldownUntil = 0;
    }

    public boolean isLockedInAction() {
        return action == ActionType.LIGHT || action == ActionType.HEAVY || action == ActionType.KICK
            || action == ActionType.SPECIAL || action == ActionType.ULTIMATE || action == ActionType.HURT;
    }

    public boolean isAlive() { return health > 0; }
    public boolean isBlocking() { return blocking; }
    public boolean isCrouching() { return crouching; }
    public boolean isStunned(long nowMs) { return nowMs < stunUntil; }
    public String getName() { return stats.name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return stats.maxHealth; }
    public int getMana() { return mana; }
    public int getMaxMana() { return stats.maxMana; }
    public int getSpecialCost() { return stats.specialCost; }
    public int getUltimateCost() { return stats.ultimateCost; }
    public int getRoundsWon() { return roundsWon; }
    public boolean isManaFull() { return mana >= stats.maxMana; }
    public boolean isSpecialReady() { return mana >= stats.specialCost && System.currentTimeMillis() >= specialCooldownUntil; }
    public boolean isUltimateReady() { return mana >= stats.ultimateCost && System.currentTimeMillis() >= ultimateCooldownUntil; }
    public long getManaFullGlowUntil() { return manaFullGlowUntil; }
    public void addRoundWin() { roundsWon++; }
    public void clearRounds() { roundsWon = 0; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getCenterX() { return x + WIDTH / 2.0; }
    public boolean isAiControlled() { return aiControlled; }
    public void setAiControlled(boolean aiControlled) { this.aiControlled = aiControlled; }
    public int getComboCounter() { return comboCounter; }
    public String getSpecialName() { return stats.specialName; }
    public String getUltimateName() { return stats.ultimateName; }
    public String getSpecialTag() { return stats.specialTag; }
    public String getUltimateTag() { return stats.ultimateTag; }
    public long getSpecialCooldownRemaining(long nowMs) { return Math.max(0, specialCooldownUntil - nowMs); }
    public long getUltimateCooldownRemaining(long nowMs) { return Math.max(0, ultimateCooldownUntil - nowMs); }
}
