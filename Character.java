package models;

public abstract class Character {
    private String name;
    protected int hp, maxHp, mana, maxMana;

    public Character(String name, int hp, int mana) {
        this.name = name;
        this.hp = hp;
        this.maxHp = hp;
        this.mana = mana;
        this.maxMana = mana;
    }

    // Encapsulation: Stats are private/protected; accessed via getters
    public String getName() { return name; }
    public int getHp() { return hp; }
    public int getMana() { return mana; }
    public boolean isAlive() { return hp > 0; }

    public void takeDamage(int dmg) {
        this.hp = Math.max(0, this.hp - dmg);
    }

    // OOP Concept: Method to be overridden (Polymorphism)
    public abstract String useSkill(int skillNum, Character target) throws Exception;
}