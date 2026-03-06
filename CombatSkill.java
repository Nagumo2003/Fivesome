package models.strategies;
import models.*;
import models.Character;
public class CombatSkill implements SkillStrategy {
    private String name;
    private int min, max, cost, regen;
    private boolean isHeal;
    public CombatSkill(String n, int min, int max, int cost, int regen, boolean h) {
        this.name = n; this.min = min; this.max = max; this.cost = cost; this.regen = regen; this.isHeal = h;
    }
    public String execute(Character u, Character t) throws Exception {
        if (u.getMana() < cost) throw new Exception("Insufficient Mana!");
        int roll = (int)(Math.random() * (max - min + 1)) + min;
        u.setMana(u.getMana() - cost + regen);
        if (isHeal) { u.setHp(u.getHp() + roll); return u.getName() + " " + CombatLogger.getFlavorText(name, roll, true); }
        else { t.takeDamage(roll); return u.getName() + " " + CombatLogger.getFlavorText(name, roll, false); }
    }
    public String getName() { return name; }
}