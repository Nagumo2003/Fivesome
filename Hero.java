package models;
import java.util.Random;

public class Hero extends Character {
    private Random rand = new Random();

    public Hero(String name, int hp, int mana) {
        super(name, hp, mana);
    }

    @Override
    public String useSkill(int skillNum, Character target) throws Exception {
        // Exception Handling: Check mana before acting
        int cost = (skillNum == 3) ? 150 : 50;
        if (this.mana < cost) {
            throw new Exception("Insufficient Mana! Need " + cost + " MP.");
        }

        switch (skillNum) {
            case 1: // Mana Regen skill from notes
                this.mana += 80;
                return getName() + " recharged Mana!";
            case 2: // Mid-tier damage
                this.mana -= 100;
                int d2 = rand.nextInt(100) + 100;
                target.takeDamage(d2);
                return getName() + " dealt " + d2 + " damage!";
            case 3: // Ultimate skill
                this.mana -= 150;
                int d3 = rand.nextInt(150) + 200;
                target.takeDamage(d3);
                return getName() + " unleashed ULTIMATE for " + d3 + " damage!";
            default:
                return getName() + " is waiting...";
        }
    }
}