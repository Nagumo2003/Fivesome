package models;
public class CombatLogger {
    public static String getFlavorText(String skill, int roll, boolean isHeal) {
        if (isHeal) return "recovered " + roll + " HP!";
        return "dealt " + roll + " damage with " + skill + "!";
    }
}