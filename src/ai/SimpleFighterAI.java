package ai;

import fighters.AttackResult;
import fighters.Fighter;
import java.util.Random;

public class SimpleFighterAI {
    private final Random random = new Random();
    private long nextDecisionTime = 0;

    public AttackResult update(Fighter self, Fighter target, long nowMs) {
        return update(self, target, nowMs, 1);
    }

    public AttackResult update(Fighter self, Fighter target, long nowMs, int difficultyTier) {
        if (nowMs < nextDecisionTime || !self.isAlive() || !target.isAlive()) return null;

        difficultyTier = Math.max(1, Math.min(5, difficultyTier));
        double distance = target.getCenterX() - self.getCenterX();
        self.startMoveLeft(false);
        self.startMoveRight(false);
        self.setBlocking(false);
        self.setCrouching(false);

        if (self.isStunned(nowMs)) {
            nextDecisionTime = nowMs + 120;
            return null;
        }

        double blockChance = 0.18 + difficultyTier * 0.04;
        double specialChance = 0.22 + difficultyTier * 0.04;
        double ultimateChance = 0.16 + difficultyTier * 0.03;
        int approachDistance = 145 - difficultyTier * 5;
        int thinkDelay = Math.max(95, 180 - difficultyTier * 18);

        if (self.getHealth() < self.getMaxHealth() * 0.4 && Math.abs(distance) < 115 && random.nextDouble() < blockChance) {
            self.setBlocking(true);
            nextDecisionTime = nowMs + 120;
            return null;
        }

        if (Math.abs(distance) > approachDistance) {
            if (distance > 0) self.startMoveRight(true);
            else self.startMoveLeft(true);
            if (random.nextDouble() < 0.05 + difficultyTier * 0.015) self.jump(nowMs);
            nextDecisionTime = nowMs + 60;
            return null;
        }

        nextDecisionTime = nowMs + thinkDelay + random.nextInt(80);
        double roll = random.nextDouble();
        if (roll < blockChance * 0.45) {
            self.setBlocking(true);
            return null;
        }
        if (self.isUltimateReady() && Math.abs(distance) < 98 && (target.getHealth() < target.getMaxHealth() * 0.55 || random.nextDouble() < ultimateChance)) {
            return self.ultimateAttack(nowMs);
        }
        if (self.isSpecialReady() && Math.abs(distance) < 108 && random.nextDouble() < specialChance) {
            return self.specialAttack(nowMs);
        }
        if (roll < 0.46) {
            return self.lightAttack(nowMs);
        } else if (roll < 0.76) {
            return self.kickAttack(nowMs);
        } else {
            return self.heavyAttack(nowMs);
        }
    }
}
