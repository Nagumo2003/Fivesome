package models;
import models.strategies.CombatSkill;

public class CharacterFactory {
    public static Hero createHero(String name) {
        Hero h;
        switch (name) {
            case "Christopher":
                h = new Hero("Christopher", 1800, 800);
                h.addSkill(new CombatSkill("Omniheal", 200, 250, 0, 50, true));
                h.addSkill(new CombatSkill("Parry", 100, 200, 100, 0, false));
                h.addSkill(new CombatSkill("Pyroburst", 200, 300, 250, 0, false));
                break;
            case "Punch":
                h = new Hero("Punch", 950, 450);
                h.addSkill(new CombatSkill("Blink Assault", 0, 50, 0, 80, false));
                h.addSkill(new CombatSkill("Death Strike", 200, 300, 95, 0, false));
                h.addSkill(new CombatSkill("Reaper's Embrace", 400, 550, 120, 0, false));
                break;
            case "Jason Tulfo":
                h = new Hero("Jason Tulfo", 800, 200);
                h.addSkill(new CombatSkill("Ipa-Tulfo kita!", 15, 60, 0, 50, false));
                h.addSkill(new CombatSkill("CCTV Reveal", 60, 120, 100, 0, false));
                h.addSkill(new CombatSkill("Sumbungan ng Bayan", 120, 300, 150, 0, false));
                break;
            case "Wilson":
                h = new Hero("Wilson", 500, 300);
                h.addSkill(new CombatSkill("Mother Hen Guard", 20, 70, 0, 150, false));
                h.addSkill(new CombatSkill("Eggbomb", 70, 120, 100, 0, false));
                h.addSkill(new CombatSkill("Ultimate Cock-a-Doodle", 120, 220, 160, 0, false));
                break;
            case "Earl":
                h = new Hero("Earl", 3000, 600);
                h.addSkill(new CombatSkill("Infernal Stigma", 300, 400, 0, 20, false));
                h.addSkill(new CombatSkill("Lightning Blade", 500, 700, 100, 0, false));
                h.addSkill(new CombatSkill("Astral Combo", 850, 1100, 200, 0, false));
                break;
            case "Sana":
                h = new Hero("Sana", 2600, 950);
                h.addSkill(new CombatSkill("Dark Flare", 300, 450, 25, 0, false));
                h.addSkill(new CombatSkill("Curse Field", 500, 700, 120, 0, false));
                h.addSkill(new CombatSkill("Abyssal Catastrophe", 1000, 1300, 320, 0, false));
                break;
            case "Papi Robinhood":
                h = new Hero("Papi Robinhood", 2000, 800);
                h.addSkill(new CombatSkill("Yessss!", 80, 190, 0, 40, false));
                h.addSkill(new CombatSkill("Happiness", 190, 400, 90, 0, false));
                h.addSkill(new CombatSkill("Array Ko", 400, 600, 120, 0, false));
                break;
            case "Lixcia":
                h = new Hero("Lixcia", 2200, 1000);
                h.addSkill(new CombatSkill("Realm Expulsion", 100, 230, 0, 67, false));
                h.addSkill(new CombatSkill("Health Destruction", 230, 400, 100, 0, false));
                h.addSkill(new CombatSkill("Project Destruction", 400, 700, 250, 0, false));
                break;
            case "Manny Bangay":
                h = new Hero("Manny Bangay", 1900, 600);
                h.addSkill(new CombatSkill("Iron Fist Jab", 60, 120, 60, 0, false));
                h.addSkill(new CombatSkill("Frying Pan", 80, 100, 65, 0, false));
                h.addSkill(new CombatSkill("Rusty Metal Combo", 100, 120, 80, 0, false));
                break;
            case "Marytes":
                h = new Hero("Marytes", 800, 600);
                h.addSkill(new CombatSkill("Tongue-linaaa!!!", 80, 165, 80, 0, false));
                h.addSkill(new CombatSkill("Rapid Tongue", 60, 120, 50, 0, false));
                h.addSkill(new CombatSkill("Burst Shout", 100, 165, 70, 0, false));
                break;
            default:
                h = new Hero("Dummy", 1000, 500);
        }
        return h;
    }
}