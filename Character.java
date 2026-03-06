package models;
import java.util.*;
import models.strategies.SkillStrategy;
public abstract class Character {
    protected String name;
    protected int hp, maxHp, mana, maxMana;
    protected List<SkillStrategy> skills = new ArrayList<>();
    public Character(String n, int hp, int mana) { this.name = n; this.hp = this.maxHp = hp; this.mana = this.maxMana = mana; }
    public void addSkill(SkillStrategy s) { skills.add(s); }
    public String useSkill(int i, Character t) throws Exception { return skills.get(i).execute(this, t); }
    public String getSkillName(int i) { return skills.get(i).getName(); }
    public void takeDamage(int d) { this.hp = Math.max(0, this.hp - d); }
    public void setHp(int h) { this.hp = Math.min(h, maxHp); }
    public void setMana(int m) { this.mana = Math.min(m, maxMana); }
    public String getName() { return name; }
    public int getHp() { return hp; } public int getMaxHp() { return maxHp; }
    public int getMana() { return mana; } public int getMaxMana() { return maxMana; }
    public boolean isAlive() { return hp > 0; }
}